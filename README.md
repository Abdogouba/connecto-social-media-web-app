# Connecto Social Media Web App Backend

Similar to Twitter or X but without media (photos/videos).

## Status
In progress, more than half of the planned requirements are done.

## Technologies and Tools
Java - Spring Boot - PostgreSQL - JWT authentication - Docker - Pagination - DB indexing - Lombok - Maven - Intellij

## Architecture and Design
controller -> controller advice (exception handler) -> service (interfaces and implementations - dto conversion) -> repository -> DB <- entities

## App Roles
User - Admin - Super admin

## Database Schema
note: this is the plan, some entities are not implemented in code yet.
1.	User (id [PK], name [not null], email [unique] [not null], password [not null], gender [not null], birth_date, location, bio, is_private [default false] [not null], is_banned [default false] [not null], role [default user], createdAt)
2.	Post (id [PK], created_at, content [not null], user_id [FK] [not null] [on delete cascade])
3.	Repost (id [PK], created_at, user_id [FK] [not null] [on delete cascade], post_id [FK] [not null] [on delete cascade])
4.	Comment (id [PK], created_at, content [not null], user_id [FK] [not null] [on delete cascade], post_id [FK] [not null] [on delete cascade])
5.	Saved post (id [PK], created_at, user_id [FK] [not null] [on delete cascade], post_id [FK] [not null] [on delete cascade]) user_id and post_id [unique]
6. Post Reaction (id [PK], user_id [FK] [not null] [on delete cascade], post_id [FK] [not null] [on delete cascade], type [not null], createdAt) user_id and post_id [unique]
7.	Liked comment (id [PK], user_id [FK] [not null] [on delete cascade], comment_id [FK] [not null] [on delete cascade]) user_id and comment_id [unique]
8.	Disliked comment (id [PK], user_id [FK] [not null] [on delete cascade], comment_id [FK] [not null] [on delete cascade]) user_id and comment_id [unique]
9.	Follow (id [PK], user_id-follower [FK] [not null] [on delete cascade], user_id-followed [FK] [not null] [on delete cascade], createdAt) user_id and user_id [unique], [check follower <> followed]
10.	Follow request (id [PK], created_at, user_id-requested [FK] [not null] [on delete cascade], user_id-reciever [FK] [not null] [on delete cascade]) user_id and user_id [unique] , [check follower <> followed]
11.	Block (id [PK], user_id-blocker [FK] [not null] [on delete cascade], user_id-blocked [FK] [not null] [on delete cascade], createdAt) user_id and user_id [unique], [check blocker <> blocked]
12.	Post Report (id [PK], created_at, reason [not null], user_id [FK] [not null] [on delete cascade], post_id [FK] [not null] [on delete cascade]) user_id and post_id [unique]
13.	Comment Report (id [PK], created_at, reason [not null], user_id [FK] [not null] [on delete cascade], comment_id [FK] [not null] [on delete cascade]) user_id and comment_id [unique]
14.	Profile Report (id [PK], created_at, reason [not null], user_id-creator [FK] [not null] [on delete cascade], user_id-profile [FK] [not null] [on delete cascade]) user_id and user_id [unique], [check creator <> profile]
15.	Notification (id [PK], user_id-receiver [FK] [not null] [on delete cascade], user_id-sender [FK] [Not Null] [on delete cascade], type [not null], reference_id, is_read [default false], created_at)

## Requirements Done
register - login - forgot password (receive a new password on email) - change password

create post (only text, no media) - edit my post

repost - delete repost - view reposts of a post - save/unsave post - like post

block profile - unblock profile - view blocked profiles

follow - unfollow - remove follower (if user is private) - view following list - view followers list - view follow requests received (if user is private) - respond to follow requests (if user is private) - view follow requests sent - cancel follow request sent - suggested to follow (profiles your following follow)

sending notifications - get my notifications - count unread notifications - mark my notifications as read

## Testing
Integration tests (MockMVC): all API endpoints implemented are tested.
