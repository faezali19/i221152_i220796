const admin=require('firebase-admin');
const fs=require('node:fs'),path=require('node:path'),crypto=require('node:crypto');
const configPath=path.join(__dirname,'../local.config.json');
const config=fs.existsSync(configPath)?JSON.parse(fs.readFileSync(configPath,'utf8')):{};
if(process.env.FIREBASE_DATABASE_EMULATOR_HOST) admin.initializeApp({projectId:'demo-kinnect',databaseURL:'https://demo-kinnect.firebaseio.com'});
else {
 if(!config.projectId||!config.databaseURL)throw Error('Run menu 5 to configure the local server first.');
 const account=JSON.parse(fs.readFileSync(path.join(__dirname,'../service-account.json'),'utf8'));
 if(account.project_id!==config.projectId)throw Error('Service account and Android Firebase projects differ.');
 admin.initializeApp({credential:admin.credential.cert(account),databaseURL:config.databaseURL,projectId:config.projectId});
}
const db=admin.database();
class HttpsError extends Error {constructor(code,message){super(message);this.code=code}}
const {RtcTokenBuilder,RtcRole}=require('agora-token');
const appId={value:()=>config.agoraAppId||''},cert={value:()=>config.agoraCertificate||''};
const mediaRoot=path.join(__dirname,'../local-media');
function mediaFile(id){if(!/^[a-f0-9]{64}$/.test(id))throw new HttpsError('invalid-argument','Invalid media identifier');return path.join(mediaRoot,id)}
const get=async p=>(await db.ref(p).get()).val();
const fail=(s,code='invalid-argument')=>{throw new HttpsError(code,s)};
const str=(v,max=2000)=>typeof v==='string'?v.trim().slice(0,max):'';
const key=v=>{if(typeof v!=='string'||!/^[\w-]{1,160}$/.test(v))fail('Invalid identifier');return v};
const fresh=()=>db.ref().push().key;
const user=async u=>{const p=await get(`users/${u}`);if(!p)fail('Complete your profile');return p};
async function chat(u,id){const c=await get(`chats/${key(id)}`);if(!c?.members?.[u])fail('Conversation access denied','permission-denied');return c;}
async function post(u,id){const p=await get(`posts/${key(id)}`);if(!p)fail('Post unavailable');if(p.ownerId!==u&&!await get(`friends/${u}/${p.ownerId}`))fail('Post access denied','permission-denied');return p;}
async function media(u,m){
 if(!m)return null;
 const id=String(m.path||'').split('/').pop(),file=mediaFile(id);
 if(!fs.existsSync(file+'.json'))fail('Upload unavailable');
 const meta=JSON.parse(fs.readFileSync(file+'.json','utf8'));
 if(meta.owner!==u||m.path!==`media/${u}/${id}`)fail('Upload owner mismatch','permission-denied');
 return {path:m.path,type:meta.type,url:`http://127.0.0.1:8080/media/${id}`};
}
async function notify(u,from,text,type,extra={}){if(u!==from){const id=fresh();await db.ref(`notifications/${u}/${id}`).set({id,from,text,type,createdAt:Date.now(),...extra})}}
const handlers={
 async register(u,d,auth){if(await get(`users/${u}`))return {};const name=str(d.name,80);if(!name)fail('Enter your name');await db.ref().update({[`users/${u}`]:{id:u,name,nameLower:name.toLowerCase(),username:'',bio:'',setupComplete:false,createdAt:Date.now()},[`privateUsers/${u}`]:{email:auth.token.email||'',birthday:str(d.birthday,32),gender:str(d.gender,32)}});return {};},
 async profile(u,d){const old=await user(u),name=str(d.name,80),username=str(d.username,30).toLowerCase().replace(/^@/,'');if(!name||!/^[a-z0-9_]{3,30}$/.test(username)||!str(d.bio))fail('Add name, bio and username (3–30 letters, digits or underscores)');const claim=await db.ref(`usernames/${username}`).transaction(v=>!v||v===u?u:undefined);if(!claim.committed)fail('Username already taken');const update={[`users/${u}`]:{...old,name,nameLower:name.toLowerCase(),username,bio:str(d.bio,250),website:str(d.website,250),avatar:d.avatar?await media(u,d.avatar):old.avatar||null,cover:d.cover?await media(u,d.cover):old.cover||null,setupComplete:true},[`privateUsers/${u}/phone`]:str(d.phone,40),[`privateUsers/${u}/gender`]:str(d.gender,32)};if(old.username&&old.username!==username)update[`usernames/${old.username}`]=null;await db.ref().update(update);return {};},
 async friend(u,d){const other=key(d.other);if(other===u)fail('Choose another user');await user(other);const op=d.operation;if(op==='request'){if(await get(`friends/${u}/${other}`))return {};await db.ref().update({[`requests/${other}/${u}`]:{id:u,createdAt:Date.now()},[`sent/${u}/${other}`]:true});await notify(other,u,`${(await user(u)).name} sent you a friend request.`,'friend',{other:u});}
 else if(op==='accept'){if(!await get(`requests/${u}/${other}`))fail('No pending request');await db.ref().update({[`friends/${u}/${other}`]:true,[`friends/${other}/${u}`]:true,[`requests/${u}/${other}`]:null,[`sent/${other}/${u}`]:null,[`requests/${other}/${u}`]:null,[`sent/${u}/${other}`]:null});await notify(other,u,`${(await user(u)).name} accepted your request.`,'accepted',{other:u});for(const [owner,viewer] of [[u,other],[other,u]]){const stories=(await db.ref('stories').orderByChild('ownerId').equalTo(owner).get()).val()||{};for(const [id,s] of Object.entries(stories))if(s.expiresAt>Date.now()&&s.audience==='friends')await db.ref(`storyFeeds/${viewer}/${id}`).set(s)}}
 else if(op==='delete'){await db.ref().update({[`requests/${u}/${other}`]:null,[`sent/${other}/${u}`]:null,[`requests/${other}/${u}`]:null,[`sent/${u}/${other}`]:null});}
 else if(op==='close'){if(!await get(`friends/${u}/${other}`))fail('Only friends can be close friends');await db.ref(`closeFriends/${u}/${other}`).set(d.enabled?true:null)}else fail('Unknown action');return {};},
 async createPost(u,d){const text=str(d.text,5000),images=[];for(const m of (d.media||[]).slice(0,20)){const f=await media(u,m);if(!f.type.startsWith('image/'))fail('Choose images for a post');images.push(f)}if(!text&&!images.length)fail('Write text or select images');const id=fresh();await db.ref(`posts/${id}`).set({id,ownerId:u,text,images,createdAt:Date.now()});return {id}},
 async react(u,d){await post(u,d.postId);if(!['like','love','haha','wow','sad','angry'].includes(d.reaction))fail('Unknown reaction');await db.ref(`posts/${d.postId}/reactions/${u}`).transaction(v=>v===d.reaction?null:d.reaction);return {}},
 async comment(u,d){const p=await post(u,d.postId),text=str(d.text),image=await media(u,d.media),parentId=d.parentId?key(d.parentId):'';if(!text&&!image)fail('Enter a comment');if(image&&!image.type.startsWith('image/'))fail('Choose an image');if(parentId&&!await get(`posts/${d.postId}/comments/${parentId}`))fail('Reply target unavailable');const id=fresh();await db.ref(`posts/${d.postId}/comments/${id}`).set({id,ownerId:u,text,image,parentId,createdAt:Date.now()});await notify(p.ownerId,u,`${(await user(u)).name} commented on your post.`,'comment',{postId:d.postId});return {id}},
 async likeComment(u,d){await post(u,d.postId);const p=`posts/${d.postId}/comments/${key(d.commentId)}`;if(!await get(p))fail('Comment unavailable');await db.ref(`${p}/likes/${u}`).transaction(v=>v?null:true);return {}},
 async openChat(u,d){const other=key(d.other);if(other===u)fail('Choose another user');await user(other);const id=[u,other].sort().join('_');await db.ref(`chats/${id}`).transaction(v=>v||{id,members:{[u]:true,[other]:true}});await db.ref().update({[`userChats/${u}/${id}/other`]:other,[`userChats/${other}/${id}/other`]:u});return {id}},
 async message(u,d){const chatId=key(d.chatId);await chat(u,chatId);const text=str(d.text,4000),image=await media(u,d.media);let sharedPost=null,reply=null;if(d.postId){const p=await post(u,d.postId);sharedPost={id:p.id,text:p.text,image:p.images?.[0]||null}}if(!text&&!image&&!sharedPost)fail('Enter a message');if(image&&!image.type.startsWith('image/'))fail('Choose an image');if(d.replyId){const r=await get(`messages/${chatId}/${key(d.replyId)}`);if(!r)fail('Reply unavailable');reply={text:r.text||'[Image]',id:r.id}}const id=d.requestId?key(d.requestId):fresh();await db.ref(`messages/${chatId}/${id}`).transaction(v=>v||{id,ownerId:u,text,image,sharedPost,reply,createdAt:Date.now(),kind:'message'});return {id}},
 async changeMessage(u,d){const cid=key(d.chatId),id=key(d.messageId);await chat(u,cid);const r=await db.ref(`messages/${cid}/${id}`).transaction(m=>{if(!m)return null;if(m.ownerId!==u||m.kind!=='message'||m.deleted||Date.now()-m.createdAt>=300000)return;if(d.operation==='delete')return {id,ownerId:u,text:'Message deleted',kind:'message',deleted:true,createdAt:m.createdAt};if(d.operation==='edit'&&str(d.text))return {...m,text:str(d.text,4000),edited:true};return});if(!r.committed||!r.snapshot.exists())fail('Only your own messages can be changed within 5 minutes','permission-denied');return {}},
 async heart(u,d){await chat(u,d.chatId);const p=`messages/${d.chatId}/${key(d.messageId)}`;if(!await get(p))fail('Message unavailable');await db.ref(`${p}/hearts/${u}`).transaction(v=>v?null:true);return {}},
 async readChat(u,d){await chat(u,d.chatId);await db.ref(`userChats/${u}/${d.chatId}/lastReadAt`).set(Date.now());return {}},
 async screenshot(u,d){await chat(u,d.chatId);const id=fresh();await db.ref(`messages/${d.chatId}/${id}`).set({id,ownerId:u,text:`${(await user(u)).name} took a screenshot`,kind:'system',createdAt:Date.now()});return {id}},
 async story(u,d){const m=await media(u,d.media);if(!m)fail('Choose media');const id=fresh(),audience=d.audience==='close'?'close':'friends',s={id,ownerId:u,media:m,label:str(d.label,160),audience,createdAt:Date.now(),expiresAt:Date.now()+86400000},f=await get(`friends/${u}`)||{},c=await get(`closeFriends/${u}`)||{},updates={[`stories/${id}`]:s};for(const v of [u,...Object.keys(f).filter(x=>audience==='friends'||c[x])])updates[`storyFeeds/${v}/${id}`]=s;await db.ref().update(updates);return {id}},
 async viewStory(u,d){const id=key(d.storyId),s=await get(`storyFeeds/${u}/${id}`);if(!s||s.expiresAt<=Date.now())fail('Story expired');await db.ref(`storyViews/${id}/${u}`).set(Date.now());return {}},
 async storyReaction(u,d){const id=key(d.storyId),s=await get(`storyFeeds/${u}/${id}`);if(!s||s.expiresAt<=Date.now())fail('Story expired');if(!['like','love','haha'].includes(d.reaction))fail('Invalid reaction');await db.ref(`storyReactions/${id}/${u}`).set(d.reaction);return {}},
 async startCall(u,d){const c=await chat(u,d.chatId),id=fresh(),other=Object.keys(c.members).find(x=>x!==u);const call={id,chatId:d.chatId,caller:u,callee:other,video:!!d.video,status:'ringing',createdAt:Date.now()};await db.ref().update({[`calls/${id}`]:call,[`incoming/${other}/${id}`]:call,[`callLogs/${u}/${id}`]:call,[`callLogs/${other}/${id}`]:call,[`callSecrets/${id}`]:{encryptionKey:crypto.randomBytes(32).toString('hex'),salt:crypto.randomBytes(32).toString('base64')}});return {id}},
 async callAction(u,d){
  const id=key(d.callId),operation=d.operation;
  const initial=await get(`calls/${id}`);
  if(!initial||![initial.caller,initial.callee].includes(u))fail('Call access denied','permission-denied');
  if(operation==='timeout'){await expireRingingCall(id);return {}}
  if(!['accept','ended','declined'].includes(operation))fail('Invalid call action');
  const result=await db.ref(`calls/${id}`).transaction(c=>{
   // null can be an unpopulated SDK cache; return null to allow a server retry.
   if(c===null)return null;
   if(!c||![c.caller,c.callee].includes(u))return;
   if(['ended','declined','missed'].includes(c.status))return;
   if(operation==='accept'&&(u!==c.callee||c.status!=='ringing'||Date.now()-c.createdAt>=30000))return;
   if(operation==='declined'&&(u!==c.callee||c.status!=='ringing'))return;
   return {...c,status:operation==='accept'?'active':operation,updatedAt:Date.now()};
  });
  if(result.committed && result.snapshot.val())await publishCallState(result.snapshot.val());
  else if(operation==='accept'){await expireRingingCall(id);fail('Call no longer ringing')}
  return {};
 },
 async callToken(u,d){const id=key(d.callId),c=await get(`calls/${id}`);if(!c||![c.caller,c.callee].includes(u)||!['ringing','active'].includes(c.status)||Date.now()-c.createdAt>7200000)fail('Call unavailable','permission-denied');if(!appId.value()||!cert.value())fail('Configure Agora credentials with menu 5 before calling');const uid=c.caller===u?1:2;return {...await get(`callSecrets/${id}`),appId:appId.value(),channel:id,uid,token:RtcTokenBuilder.buildTokenWithUid(appId.value(),cert.value(),id,uid,RtcRole.PUBLISHER,3600,3600)}},
 async listing(u,d){const title=str(d.title,120),price=str(d.price,30),image=await media(u,d.media);if(!title||!price||!image||!image.type.startsWith('image/'))fail('Add title, price and image');const id=fresh();await db.ref(`listings/${id}`).set({id,ownerId:u,title,price,image,location:str(d.location,100),category:str(d.category,40)||'Other',createdAt:Date.now()});return {id}}
};
async function push(uid,title,body,data){const tokens=Object.keys(await get(`tokens/${uid}`)||{});if(!tokens.length)return;const result=await admin.messaging().sendEachForMulticast({tokens,notification:{title,body},data,android:{priority:'high',notification:{channelId:'kinnect_messages'}}});await Promise.all(result.responses.map((r,i)=>!r.success&&['messaging/registration-token-not-registered','messaging/invalid-registration-token'].includes(r.error?.code)?db.ref(`tokens/${uid}/${tokens[i]}`).remove():Promise.resolve()));}
async function messageCreated(e){const m=e.data.val(),c=await get(`chats/${e.params.cid}`);if(!c)return;for(const uid of Object.keys(c.members)){await db.ref(`userChats/${uid}/${e.params.cid}`).transaction(v=>v?.updatedAt>m.createdAt?v:{...v,other:Object.keys(c.members).find(x=>x!==uid),lastMessage:m.text||(m.image?'Photo':'Shared post'),lastSender:m.ownerId,updatedAt:m.createdAt});if(uid!==m.ownerId)await push(uid,(await user(m.ownerId)).name,m.text||'New media message',{chatId:e.params.cid,other:m.ownerId,messageId:e.params.mid})}}
async function messageEdited(e){const a=e.data.after.val(),b=e.data.before.val();if(!a||!b||a.text===b.text)return;const c=await get(`chats/${e.params.cid}`);for(const uid of Object.keys(c?.members||{}))await db.ref(`userChats/${uid}/${e.params.cid}`).transaction(v=>v?.updatedAt===a.createdAt?{...v,lastMessage:a.text}:v)}
async function callCreated(e){const c=e.data.val();await push(c.callee,(await user(c.caller)).name,c.video?'Incoming video call':'Incoming voice call',{callId:c.id})}
// Call expiry is independent of story cleanup and notification recovery.
async function publishCallState(c){
 await db.ref().update({[`incoming/${c.callee}/${c.id}`]:null,
  [`callLogs/${c.caller}/${c.id}`]:c,[`callLogs/${c.callee}/${c.id}`]:c});
}
async function expireRingingCall(id){
 const result=await db.ref(`calls/${id}`).transaction(c=>{
  if(c===null)return null;
  if(!c||c.status!=='ringing'||Date.now()-c.createdAt<30000)return;
  return {...c,status:'missed',updatedAt:Date.now()};
 });
 if(result.committed && result.snapshot.val())await publishCallState(result.snapshot.val());
}
async function expireCalls(){
 const calls=await get('calls')||{};
 await Promise.all(Object.entries(calls)
  .filter(([,c])=>c.status==='ringing'&&Date.now()-c.createdAt>=30000)
  .map(([id])=>expireRingingCall(id)));
}

async function expiry(){const stories=(await db.ref('stories').orderByChild('expiresAt').endAt(Date.now()).get()).val()||{},updates={};if(Object.keys(stories).length){const feeds=await get('storyFeeds')||{};for(const id of Object.keys(stories)){updates[`stories/${id}`]=null;updates[`storyViews/${id}`]=null;updates[`storyReactions/${id}`]=null;for(const [uid,feed] of Object.entries(feeds))if(feed[id])updates[`storyFeeds/${uid}/${id}`]=null}}if(Object.keys(updates).length)await db.ref().update(updates);await expireCalls();}

// Durable, retryable notification jobs. Database writes precede enqueue; startup
// recovery scans recent messages/calls, so a stopped server does not lose jobs.
const runningJobs=new Set();
async function processJob(kind,id,data){
 const jobKey=kind+id;if(runningJobs.has(jobKey))return;runningJobs.add(jobKey);
 try {
 const ref=db.ref(`serverJobs/${kind}/${id}`), old=(await ref.get()).val();
 if(old?.done)return;
 try {
  if(kind==='message')await messageCreated({data:{val:()=>data.value},params:{cid:data.cid,mid:data.mid}});
  else if(kind==='call')await callCreated({data:{val:()=>data.value}});
  await ref.set({done:true,at:Date.now()});
 }catch(e){console.error('Notification pending:',e.code||e.name);}
 }finally{runningJobs.delete(jobKey)}
}
async function recoverJobs(){
 const cutoff=Date.now()-86400000;
 const chats=await get('chats')||{};
 for(const cid of Object.keys(chats)){
  const messages=(await db.ref(`messages/${cid}`).orderByChild('createdAt').startAt(cutoff).get()).val()||{};
  for(const [mid,value] of Object.entries(messages))await processJob('message',cid+'_'+mid,{cid,mid,value});
 }
 const calls=(await db.ref('calls').orderByChild('createdAt').startAt(Date.now()-60000).get()).val()||{};
 for(const [id,value] of Object.entries(calls))if(value.status==='ringing')await processJob('call',id,{value});
 const old=(await db.ref('serverJobs').get()).val()||{};
 for(const [kind,jobs] of Object.entries(old))for(const [id,v] of Object.entries(jobs))if(v.at<cutoff)await db.ref(`serverJobs/${kind}/${id}`).remove();
}
async function dispatch(uid,data,token){
 const h=Object.hasOwn(handlers,data?.action)?handlers[data.action]:null;
 if(!h)fail('Unknown action');
 let before;
 if(data.action==='changeMessage')before=await get(`messages/${key(data.chatId)}/${key(data.messageId)}`);
 const result=await h(uid,data,{uid,token});
 if(['message','screenshot'].includes(data.action)){
  const value=await get(`messages/${data.chatId}/${result.id}`);
  await processJob('message',data.chatId+'_'+result.id,{cid:data.chatId,mid:result.id,value});
 }
 if(data.action==='changeMessage'){const after=await get(`messages/${data.chatId}/${data.messageId}`);await messageEdited({data:{before:{val:()=>before},after:{val:()=>after}},params:{cid:data.chatId,mid:data.messageId}});}
 if(data.action==='startCall')await processJob('call',result.id,{value:await get(`calls/${result.id}`)});
 return result;
}
const {createServer}=require('./transport');
function server(){return createServer({
 verify:async token=>{const decoded=await admin.auth().verifyIdToken(token,true);return decoded},
 dispatch,mediaRoot,
 saveUpload:async(uid,id,type,bytes)=>{
  const file=mediaFile(id);fs.mkdirSync(mediaRoot,{recursive:true});
  fs.writeFileSync(file,bytes,{flag:'wx'});
  fs.writeFileSync(file+'.json',JSON.stringify({owner:uid,type,size:bytes.length,createdAt:Date.now()}),{flag:'wx'});
  return {path:`media/${uid}/${id}`,type,url:`http://127.0.0.1:8080/media/${id}`};
 }
})}
exports._handlers=handlers;
exports._dispatch=dispatch;
exports._expiry=expiry;
exports._recoverJobs=recoverJobs;
if(require.main===module){
 (async()=>{
  if(process.argv.includes('--rules')){
   await db.setRules(fs.readFileSync(path.join(__dirname,'../database.rules.json'),'utf8'));
   console.log('Realtime Database rules applied to '+config.projectId+'. No cloud compute or storage deployment.');
   await admin.app().delete();return;
  }
  const app=server();app.on('error',e=>{console.error('Server could not start:',e.message);process.exit(1)});
  app.listen(8080,'127.0.0.1',()=>console.log('Kinnect local backend ready on 127.0.0.1:8080. Keep this window open.'));
  let busy=false;
  async function tick(){if(busy)return;busy=true;try{await expiry();await recoverJobs()}catch(e){console.error('Maintenance:',e.code||e.message)}finally{busy=false}}
  // A slow notification retry must never block the ringing deadline.
  let expiringCalls=false;
  async function callTick(){
   if(expiringCalls)return;expiringCalls=true;
   try{await expireCalls()}catch(e){console.error('Call expiry:',e.code||e.message)}
   finally{expiringCalls=false}
  }
  callTick();setInterval(callTick,1000);
  tick();setInterval(tick,60000);
 })().catch(e=>{console.error(e.message);process.exit(1)});
}
