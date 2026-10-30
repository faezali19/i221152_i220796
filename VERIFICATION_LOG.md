# Actual team verification

Add only checks you actually performed, with date, device/API and results.

- Stage 1, 2026-10-25 11:06:58Z, Faez Ali: 13 backend tests passed; debug build succeeded; installed and opened on device 07586251BG006240. Live feature screens not tested yet.

- Stage 2, 2026-10-25 12:11:20Z, Faez Ali: On Infinix Note 11: terms checkbox worked; account creation opened Edit Profile; profile setup completed; reopening the app kept the session; logout and login worked; Back after logout did not reopen the signed-in screen.

- Stage 3, 2026-10-25 14:16:18Z, Faez Ali: On Infinix Note 11: selected and saved the avatar; it now appears on the profile. Name and bio save correctly; friend count shows 0.

- Stage 4, 2026-10-25 14:38:30Z, Faez Ali: searched for the second account and opened its profile

- Stage 5, 2026-10-25 14:45:28Z, Faez Ali: On Infinix Note 11: searched for the second account by exact and partial username; the result appeared and its profile opened. A nonexistent search showed no result without crashing.

- Stage 6, 2026-10-27 15:53:22Z, Faez Ali: On my Infinix Note 11, selected multiple gallery images and confirmed their previews in Create Post; captured a photo and confirmed its preview; recorded a video and confirmed its preview in Story Editor.

- Stage 7, 2026-10-27 17:06:40Z, Faez Ali: Verified gallery and camera photo previews, text-only and four-image post publishing, newest-first feed, +N image grid, image viewing, and posts persisting after reopening. Confirmed friends can see posts and a non-friend account cannot see them in its feed.

- Stage 8, 2026-10-27 17:16:13Z, Faez Ali: Verified all six reactions, replacing and removing my reaction, and one reaction per user per post. Tested a second friend account reacting independently; removing its reaction preserved the first account's reaction. Simultaneous two-device updates were not tested.

- Stage 9, 2026-10-28 07:38:16Z, Faez Ali: Verified text and image comments, replies, Replying to/Cancel behavior, and comment like/unlike counts. Reopened comments and confirmed saved text, images, replies, and likes persisted. Confirmed comments were visible from the second friend account.

- Stage 9, 2026-10-28 07:54:39Z, Faez Ali: Verified password show/hide on Login and Create account without changing entered text. Verified single-tap gender selection across labels, radio circles and card padding. Verified birthday boxes/arrows open the picker and retain the selected date. Confirmed terms checkbox toggles and existing-account login still works.

- Stage 10, 2026-10-28 09:20:04Z, Faez Ali: Verified photo/video story previews, captions, publishing, Home story strip and playback. Verified friend access, viewer count/list and reaction indicator. Verified close-friends access and audience exclusions.

- Stage 11, 2026-10-28 10:35:05Z, Faez Ali: Tested on Infinix Note 11: text, image and shared-post messages appear in chat; shared posts open all four images. Replies and hearts work. Own messages can be edited/deleted within five minutes, with Edited label; options disappear after five minutes. Last-message preview and unread status work. Repeated taps no longer open duplicate chats.

- Stage 12, 2026-10-29 13:41:39Z, Faez Ali: Tested Stage 12 on Infinix and Honor with separate accounts. Background message notification received; tapping it opened the correct chat. Green dot and Active now updated on foreground/background transitions. Logout showed offline status. Firebase presence changed to false approximately 90 seconds after internet disconnection and returned to true after reconnection. Last-seen text displayed correctly.

- Stage 13, 2026-10-30 15:07:48Z, Faez Ali: Tested voice and video calls between Infinix and Honor. Answer/decline, two-way audio, remote video, mute, speaker and end-call controls work. Corrected video layout displays properly. Unanswered calls now end after approximately 30 seconds.

- Stage 14, 2026-10-30 15:51:19Z, Faez Ali: Tested chat screenshots on Redmi running Android 15. Taking a screenshot created a system message with the correct profile name on both devices. The background receiver received a notification, and tapping it opened the correct conversation.
