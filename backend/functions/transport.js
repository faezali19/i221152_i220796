'use strict';
const http=require('node:http'),fs=require('node:fs'),path=require('node:path'),crypto=require('node:crypto');
const TYPES=new Set(['image/jpeg','image/png','image/webp','image/gif','image/heic','image/heif','video/mp4','video/webm','video/3gpp','video/quicktime']);
function error(status,message){return Object.assign(new Error(message),{status})}
async function body(req,max){const parts=[];let size=0;for await(const part of req){size+=part.length;if(size>max)throw error(413,'File/request is too large (media maximum 50 MiB)');parts.push(part)}return Buffer.concat(parts)}
function json(res,status,value){res.writeHead(status,{'Content-Type':'application/json','Cache-Control':'no-store','X-Content-Type-Options':'nosniff'});res.end(JSON.stringify(value))}
function createServer({verify,dispatch,mediaRoot,saveUpload}){
 const server=http.createServer(async(req,res)=>{
  try {
   // Only ADB reverse/local requests. Reject browser cross-origin writes and DNS rebinding.
   if(!['127.0.0.1:'+server.address().port,'localhost:'+server.address().port].includes(req.headers.host))throw error(403,'Invalid host');
   if(req.headers.origin)throw error(403,'Browser requests are not supported');
   if(req.method==='GET'&&req.url==='/health'){json(res,200,{status:'ok',backend:'kinnect-local-v2'});return}
   const match=/^\/media\/([a-f0-9]{64})$/.exec(req.url);
   if((req.method==='GET'||req.method==='HEAD')&&match){
    // Unguessable bearer URL is shared only in authorized RTDB records.
    const file=path.join(mediaRoot,match[1]);
    if(!fs.existsSync(file+'.json')||!fs.existsSync(file))throw error(404,'Media not found');
    const meta=JSON.parse(fs.readFileSync(file+'.json','utf8')),size=fs.statSync(file).size;
    let start=0,end=size-1,status=200;
    if(req.headers.range){
     const r=/^bytes=(\d+)-(\d*)$/.exec(req.headers.range);
     if(!r||Number(r[1])>=size||r[2]&&Number(r[2])<Number(r[1])){res.writeHead(416,{'Content-Range':`bytes */${size}`});res.end();return}
     start=Number(r[1]);end=r[2]?Math.min(Number(r[2]),size-1):size-1;status=206;
    }
    const headers={'Content-Type':meta.type,'Content-Length':end-start+1,'Accept-Ranges':'bytes','Cache-Control':'private, max-age=3600','X-Content-Type-Options':'nosniff'};
    if(status===206)headers['Content-Range']=`bytes ${start}-${end}/${size}`;
    res.writeHead(status,headers);if(req.method==='HEAD'){res.end();return}
    fs.createReadStream(file,{start,end}).on('error',()=>res.destroy()).pipe(res);return;
   }
   if(req.method!=='POST'||!['/api','/upload'].includes(req.url))throw error(404,'Unknown endpoint');
   const bearer=/^Bearer (\S+)$/.exec(req.headers.authorization||'');if(!bearer)throw error(401,'Please log in');
   let auth;try{auth=await verify(bearer[1])}catch{throw error(401,'Session expired. Log in again.')}
   if(req.url==='/api'){
    if(req.headers['content-type']!=='application/json')throw error(415,'Expected JSON');
    let data;try{data=JSON.parse((await body(req,1024*1024)).toString('utf8'))}catch(e){if(e.status)throw e;throw error(400,'Invalid JSON')}
    if(!data||Array.isArray(data)||typeof data!=='object')throw error(400,'Invalid request');
    json(res,200,await dispatch(auth.uid,data,auth));return;
   }
   const type=req.headers['content-type'];if(!TYPES.has(type))throw error(415,'Select a supported image or video');
   const bytes=await body(req,50*1024*1024);if(!bytes.length)throw error(400,'Empty file');
   json(res,200,await saveUpload(auth.uid,crypto.randomBytes(32).toString('hex'),type,bytes));
  }catch(e){if(!res.headersSent)json(res,e.status||(e.code==='permission-denied'?403:e.code==='unauthenticated'?401:e.code==='invalid-argument'?400:500),{error:e.status||e.code?e.message:'Backend error; inspect the server window.'});else res.destroy();if(!e.status&&!e.code)console.error(e.message)}
 });
 server.requestTimeout=120000;server.headersTimeout=15000;return server;
}
module.exports={createServer};
