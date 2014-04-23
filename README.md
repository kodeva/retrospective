Client-server tool for performing and documenting retrospective sessions (Scrum/Agile) in distributed teams
===========================================================================================================
By Stepan Hrbacek, Kodeva 


Terms
-----
* Session is the retrospective session.
* Card is 2-sided retrospective card with a mandatory feedback text on the front side; and optionally votes, issue analysis and action items on the back side. Cards can be of either Well-Done or Improvement type.
* User card desk is a container for cards visible to the User only.
* Pinwall is a Session container for cards visible to all Session participants of given Session.


Actors - Roles
--------------
* Client is the client part of this retrospective tool.
* Server is the server part of this retrospective tool.
* User is anyone using the Client.
* Session participant is a user connected to the Session.


Supported Use Cases
-------------------
* User creates, removes and sees his local card desks.
* User adds, removes and sees cards to/from/on his local card desks.
* User modifies a front-side text on a card on his local card desks.
* Session participant moves a card from the local card desk to the session pinwall.


Planned Use Cases
-----------------
* User creates a new session with a new pinwall, connects a local desk to the session and becomes the session participant.
* User gets invitation from the session participant, joins the session, connects a local card desk to the session and becomes the session participant.

* Session participant invites other users to join the session.
* Session participant moves a card from the session pinwall to the local card desk.
* Session participant can see both front and back side of all cards on the session pinwall, the back side only if not empty.
* Session participant adds or removes one or more of available votes to/from Improvent cards on the session pinwall. Each participant has 3 votes available in one session.
* Session participant reserves a card on the session pinwall for modification, modifies its front or back side and releases it back after modifications are done. Other session participants can immediately see all changes performed to the reserved card but cannot reserve the card until it is released.
* Session participant creates a PDF/HTML protocol for the session that contains list of session date, time, participants and pinwall cards including votes.
* Session participant can see status of other participant - actively using the client, out of the client window or disconnected.
* Session participant can see cards that were moved from the session pinwall to local desks of other session participants.

* Client persists current state of all user's local card desks and restores them on startup.
* Server persists current state of all sessions' pinwalls and restores them when users join given sessions.