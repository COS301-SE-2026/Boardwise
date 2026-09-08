# Boardwise: Software Requirements Specification

**Department of Computer Science**
**Faculty of Engineering, Built Environment & IT**
**University of Pretoria**
**COS301 - Software Engineering**

---

**Item:** Capstone 2026 - Demo 3
**Team Name:** Works On My Machine
**Team Members:**

| Name | Surname | Student Number | % Contribution |
| --- | --- | --- | --- |
| Hayley | Booysen | u24868346 | -- |
| Bandile* | Mnyandu* | u24675394* | -- |
| Karabo | Nkomo | u24865169 | -- |
| Palesa | Nkosi | u22664638 | -- |
| Njabulo | Mathonsi | u24676412 | -- |

\*- indicates team leader

---

## Table of Contents

1. [Project Owner](#1-project-owner)
2. [Introduction](#2-introduction)
3. [User Stories / User Characteristics](#3-user-stories--user-characteristics)
4. [Use Cases](#4-use-cases)
5. [Functional Requirements](#5-functional-requirements)
6. [Non-Functional Requirements](#6-non-functional-requirements-quantified-targets)
7. [Domain Model](#7-domain-model)
8. [Subsystems](#8-subsystems)
9. [Traceability Matrix](#9-traceability-matrix)

---

### 1. Project Owner

**Name:** Saskia Steyn

**Email:** saskia.steyn@labs.epiuse.com

---

### 2. Introduction

**2.1 Business Need**

The South African board gaming community is vibrant but largely offline and fragmented. Enthusiasts currently rely on isolated, retailer-specific locations to discover new games, find playing groups, and participate in organised events. This creates a dependency on physical stores and limits organic, peer-to-peer (P2P) interaction between players.

Boardwise addresses this fragmentation by providing a centralised, store-agnostic digital ecosystem tailored to the local market. The vision of Boardwise is to digitise and expand the board game experience by optimising collection management and community engagement. By consolidating game discovery, P2P logistics, and social coordination into a single interface, Boardwise empowers players and collectors to connect, trade, and organise independently of any single retailer.

**2.2 Objectives**

The core objectives of the system are to:

* **Centralise the Community:** Provide a comprehensive digital home for board game enthusiasts in South Africa to manage their personal collections and discover local players.
* **Decentralise Transactions:** Enable a secure P2P marketplace for renting and selling board games, removing the reliance on commercial retailers for game acquisition.
* **Facilitate Organised Play:** Foster community formation by allowing users to create dedicated social groups and schedule thematic board game events independently.
* **Democratise Knowledge:** Maintain "The Vault," a shared digital library that allows the community to collaboratively upload, store, edit, and maintain board game rulebooks.

**2.3 Scope of this Document (Demo 3)**

This document is an **incremental update** to the Demo 2 SRS, produced in line with the team's Agile process - each demo milestone is captured as a new, versioned SRS rather than a full rewrite. It captures the requirements, use cases, and domain model in scope for **Demo 3**, building on the carried-over Demo 1/Demo 2 baseline (authentication, profile management, groups, game inventory management, full event lifecycle management and RSVP, marketplace browsing and listing management, Vault rulebook upload/ingestion, and real-time collaborative rulebook editing) and adding the following new capability for this milestone:

* A natural language query interface for The Vault, backed by a Retrieval-Augmented Generation (RAG) pipeline, allowing users to submit gameplay questions and receive a synthesised answer parsed against the uploaded rulebooks.
* Direct user-to-user **friend connections**, including sending, accepting/rejecting, and removing a friend request, and viewing a friends list.
* A personalised **"Web" tab** within the Marketplace, populated by an automated background scraper that surfaces external retail listings (Takealot, Bobshop, ToysRUs) for the platform's most popular board games, ranked by inventory count.

The following capabilities remain explicitly **out of scope** for Demo 3 and are deferred to a future release:

* A dedicated, on-demand price-comparison feature - e.g. a user searching for and comparing live prices for any specific, arbitrary board game outside of the popularity-driven Web tab - remains deferred.
* Broader personalised recommendations beyond the popularity-driven Marketplace Web tab (e.g. suggested friends, groups, or events based on user history).
* Broader generative AI/LLM-assisted functionality beyond the rules-query assistant introduced in this milestone (e.g. a "Generative Game Architect").

---

### 3. User Stories / User Characteristics

#### 3.1 User Characteristics

The Boardwise platform utilises a simplified Role-Based Access Control (RBAC) and attribute-based visibility model consisting of two primary user types. Access to specific features and navigational paths is determined by the user's authentication status, their ownership of specific entities, and the visibility flags set within the data model.

**Guest User (Unauthenticated)**

* **Permissions:** Strictly read-only access.
* **Navigational Paths:** Limited to browsing public marketplace listings and reading `Ready`-status rulebooks within The Vault. Guest users are explicitly blocked from viewing user profiles, community groups, and events, and from all interactive features (creating listings, uploading rulebooks, RSVPing, etc.). The Marketplace's "Web" tab (external retail listings) is also gated behind login and is not part of a Guest's read-only marketplace browsing. A Guest's only interactive actions are registering a new account or logging into an existing one.

**Standard Registered User**

* **Permissions:** General authenticated access to all core platform features. A Standard Registered User acts as an event organiser, marketplace seller, or Vault contributor based purely on their system interactions and entity ownership - there is no separate "seller" or "organiser" role.
* **Visibility & Ownership Controls:**
  * **Profile & Social:** Their public data footprint is governed by the `USER.preferences.visibility` flag. If set to 'Private', their game mechanics, genres, and inventory are hidden from general queries. They can view and join groups where `visibility` is 'Public', and RSVP to events where the event `visibility` is 'Public' (or if explicitly invited to a 'Private' event).
  * **Events:** When a user creates an event, they gain exclusive ownership rights for that specific entity, allowing them to manage the `Event_Participants` list, update event details, and modify the event `status`.
  * **Marketplace:** Users can create `Listings` and manage embedded `Rental_Period` data. They hold exclusive write and delete permissions over their own marketplace listings, preventing unauthorised modification by other Standard Registered Users.
  * **The Vault:** Users are authorised to initiate an `Ingestion_Job` by uploading a PDF rulebook. They are also authorised to acquire a Multi-Reader Single-Writer (MRSW) lock on an existing `Rulebook`. While holding this lock, they gain access to the collaborative editor interface to modify `Chunk` data, which sequentially appends to the immutable `Edit_Event` ledger.

#### 3.2 User Stories

*Stories are grouped by subsystem epic. Stories marked **(New - Demo 2)** or **(New - Demo 3)** correspond to functionality introduced in that milestone; all other stories are carried over from earlier demos and included here for completeness.*

**Epic: Authentication** *(Social & Events subsystem)*

##### US-AUTH-01: Register an Account

**As a user, I want to register an account, so that I can access the Boardwise platform and its features.**

**Acceptance Criteria:**
- Given I am on the registration page, when I provide a valid username, email address, and password and submit the form, then a new account is created and I am redirected to my profile setup page.
- Given I submit the form with an email address that is already registered, then the system displays an error informing me the email is already in use.
- Given I have successfully registered, then my password is stored in an encrypted format and is never stored in plain text.

##### US-AUTH-02: Log Into an Account

**As a user, I want to log into my account, so that I can access my personalised profile and platform features.**

**Acceptance Criteria:**
- Given I provide a valid registered email and correct password, then I am authenticated, a secure JWT is issued, and I am redirected to my home feed.
- Given I provide an incorrect password or unregistered email, then the system displays a generic error message and does not grant access.

##### US-AUTH-03: Log Out of an Account

**As a user, I want to log out of my account, so that my session is secure when I am done using the platform.**

**Acceptance Criteria:**
- Given I am logged in, when I select logout, then my session is terminated, my JWT is added to the `Token_Blacklist`, and I am redirected to the login page.
- Given I have logged out, when I attempt to navigate to a protected page, then I am redirected to the login page.

---

**Epic: Profile Management** *(Social & Events subsystem)*

##### US-PROF-01: Manage My Profile

**As a user, I want to view and update my profile information, so that I can keep my details accurate and control what other users can see.**

**Acceptance Criteria:**
- Given I am on my profile page, when I select the edit option, then I can modify my display name, bio, and profile picture.
- Given I toggle my `preferences.visibility` to Private, then my game mechanics, genres, and inventory are hidden from other users' queries of my profile.
- Given I attempt to save with the display name field empty, then the system displays a validation error and does not save the changes.

##### US-PROF-02: View a Profile

**As a user, I want to view my own profile and the public profiles of other users, so that I can see their information and game collections.**

**Acceptance Criteria:**
- Given I navigate to my own profile, then I see my full details including any private fields.
- Given I navigate to another user's profile and their `visibility` is Public, then I see a read-only view of their inventory and preferences.
- Given the profile I navigate to has `visibility` set to Private, then their inventory and preferences are hidden from me.

---

**Epic: Game Inventory (New - Demo 2)**

##### US-INV-01: Manage My Game Inventory

**As a user, I want to add board games from the catalogue to my personal inventory, or remove games I no longer own, so that my collection stays accurate.**

**Acceptance Criteria:**
- Given I search for a game title, when local catalogue results are insufficient, then the system retrieves matching results via the BoardGameGeek (BGG) integration.
- Given I select a game to add, then the relationship between my `User` record and the `Board_Game` is persisted and the game appears in my inventory.
- Given I select the remove action on a game in my inventory, then the relationship is deleted and the game no longer appears in my inventory.

---

**Epic: Social - Groups**

##### US-GRP-01: Manage Groups

**As a user, I want to create a group or join an existing public group, so that I can organise or participate in a dedicated community space.**

**Acceptance Criteria:**
- Given I provide a group name and submit, then a new group is created with me as the owner and I am automatically added as a member.
- Given I browse a group with `visibility` set to Public, when I select join, then I am added as a member.
- Given a group has `visibility` set to Private and I am not a member, then the group is not visible to me in search or browse results.

---

**Epic: Social - Friends (New - Demo 3)**

##### US-SOC-01: Send a Friend Request

**As a user, I want to send a friend request to another user, so that I can connect with them on the platform.**

**Acceptance Criteria:**
- Given I am viewing another user's profile, when I select the add friend option, then a friend request is sent to that user.
- Given I have already sent a friend request to a user, when I view their profile, then the add friend option is replaced with a pending status indicator.
- Given the other user has already sent me a friend request, when I attempt to send one to them, then the system instead presents me with the option to accept their existing request.

##### US-SOC-02: Accept or Reject a Friend Request

**As a user, I want to accept or reject incoming friend requests, so that I can control who is in my friend network.**

**Acceptance Criteria:**
- Given I have received a friend request, when I navigate to my notifications or friend requests page, then I can see the request with options to accept or reject.
- Given I accept a friend request, then both users are added to each other's friends list.
- Given I reject a friend request, then the request is removed and the requesting user is not added to my friends list.

##### US-SOC-03: View Friends List

**As a user, I want to view my friends list, so that I can see all the users I am connected with.**

**Acceptance Criteria:**
- Given I am on my profile or friends page, when I navigate to my friends list, then I can see all users I am currently friends with, including their display names and profile pictures.
- Given I have no friends added, when I navigate to my friends list, then the system displays a message indicating my friends list is empty.

##### US-SOC-04: Unfriend a User

**As a user, I want to remove a user from my friends list, so that I can manage my social connections on the platform.**

**Acceptance Criteria:**
- Given I am viewing my friends list or a friend's profile, when I select the unfriend option, then the system prompts me to confirm the action.
- Given I confirm the unfriend action, then the user is removed from my friends list and I am removed from theirs.
- Given I have unfriended a user, when I view their profile, then the add friend option is displayed again.

---

**Epic: Community - Events (New - Demo 2)**

##### US-EVT-01: Manage My Events

**As a user, I want to schedule a new gaming event or edit the details of an event I created, so that I can organise a session and keep its information accurate.**

**Acceptance Criteria:**
- Given I provide a name, date, time, location (validated via the Google Maps integration), game, and visibility (Public or Private), then a new `Events` entity is created and I am automatically added as a participant.
- Given I am the creator of an event, when I edit its details and save, then the entity is updated and existing participants are notified of the change.
- Given I am not the creator of an event, then no edit option is presented to me.

##### US-EVT-02: View Events

**As a user, I want to browse available gaming events, so that I can find sessions to join.**

**Acceptance Criteria:**
- Given I browse the events list, then I see all Public events and any Private events I have been explicitly invited to.
- Given there are no visible events, then the system displays a message indicating none are currently scheduled.

##### US-EVT-03: RSVP to an Event

**As a user, I want to join or withdraw from a gaming event, so that the organiser knows whether I plan to attend.**

**Acceptance Criteria:**
- Given I view a Public event, or a Private event I was invited to, when I select "Join", then my `Event_Participants` record is created and I appear on the attendee list.
- Given I have already joined an event, when I select "Withdraw", then my `Event_Participants` record is removed and the join option is restored.

---

**Epic: Marketplace**

##### US-MKT-01: Browse and Filter Listings

**As a board game enthusiast, I want to browse and filter listings created by other users, so that I can find board games available to rent or buy within my community.**

**Acceptance Criteria:**
- Given I open the marketplace, then all active listings are displayed showing game title, listing type (rent/sale), price, and availability.
- Given I filter by listing type, price range, or game title, then only matching listings are displayed.
- Given no listings match my filters, then a "No listings found" message is displayed.

##### US-MKT-02: View Listing Detail

**As a prospective buyer or renter, I want to view the full detail of a listing, so that I can decide whether to contact the seller.**

**Acceptance Criteria:**
- Given I select a listing, then I am shown its full description, price, rental period (if applicable), and the seller's display name.

##### US-MKT-03: Manage My Listings

**As a board game owner, I want to create, edit, and delete my own rental or sale listings, so that I can keep my availability and pricing accurate.**

**Acceptance Criteria:**
- Given I submit a valid listing form (game title, listing type, price, and - for rentals - a `Rental_Period`), then the listing is immediately published and visible in the marketplace.
- Given I select "Edit" on one of my own listings, then a pre-populated form is shown and my changes are persisted on save.
- Given I select "Delete" on one of my own listings, then it is immediately removed from the public marketplace.
- Given I attempt to edit or delete a listing that belongs to another user, then the action is rejected with an authorisation error.

---

**Epic: The Vault - Ingestion & Browsing**

##### US-VLT-01: Upload a Rulebook (New - Demo 2)

**As a community contributor, I want to upload a PDF rulebook, so that it can be added to the Shared Library for others to view and edit.**

**Acceptance Criteria:**
- Given I select a PDF file under 50 MB and a linked `game_id`, when I submit, then the system returns `202 Accepted` immediately and processes sanitisation, extraction, and storage in the background.
- Given my file exceeds 50 MB, is not a PDF, or fails sanitisation, then the upload is rejected with a clear error (`413`, `415`, or `422`).
- Given ingestion completes successfully, then the `Ingestion_Job` status transitions to `Ready` and the rulebook becomes visible in Vault search results.

##### US-VLT-02: Browse the Vault Library

**As a tabletop player, I want to search for and view rulebooks in the Vault, so that I can find the rules for a specific game.**

**Acceptance Criteria:**
- Given I search by game title, then only `Ready`-status rulebooks matching my query are returned.
- Given I am an unauthenticated Guest, then I can still browse and read `Ready`-status rulebooks.

---

**Epic: The Vault - Collaborative Editing (New - Demo 2)**

##### US-VLT-03: Edit a Rulebook

**As a community contributor, I want to edit a section of a rulebook, so that I can correct an error or add official errata.**

**Acceptance Criteria:**
- Given I hold the active MRSW write lock for the rulebook, when I modify, insert, or delete a `Chunk`, then the change is validated against the `expectedVersion`, persisted, and appended to the `Edit_Event` ledger.
- Given the `expectedVersion` I submit does not match the server's current version, then the edit is rejected with a `409 Conflict`.
- Given I commit a change, then all other active readers see the update reflected in real time via a WebSocket broadcast.

##### US-VLT-04: Undo and Redo My Edits

**As a contributor actively editing a rulebook, I want to undo my most recent edit or redo an undone edit, so that I can correct mistakes without manually retyping text.**

**Acceptance Criteria:**
- Given my undo stack is not empty, when I trigger "Undo", then the inverse operation is applied to the affected `Chunk`, a compensating `Edit_Event` entry is written, and the `Rulebook` version is incremented.
- Given I have just undone an edit, when I trigger "Redo", then the original operation is reapplied.
- Given my undo (or redo) stack is empty, when I trigger the corresponding action, then the system ignores the request without error.

##### US-VLT-05: View Rulebook Edit History

**As a Vault user, I want to view the full chronological edit history of a rulebook, so that I can audit what has changed and by whom.**

**Acceptance Criteria:**
- Given I select "View History" on a rulebook, then the system retrieves the immutable `Edit_Event` ledger and renders an ordered list.
- Given the history is displayed, then each entry shows the `editType`, target `chunkId`, editor's username, before/after text, and resulting `versionPostEdit`.

##### US-VLT-06: Concurrency Lock Management

**As a contributor, I want the system to manage the MRSW write lock automatically, so that two people can never edit a rulebook at the same time and locks don't get stuck.**

**Acceptance Criteria:**
- Given no other user holds the lock, when I request to edit, then I am granted the exclusive write lock and gain access to the editor.
- Given I finish editing and voluntarily release the lock, then the system clears the lock and broadcasts a `voluntary` release event to waiting users.
- Given I hold the lock and my session disconnects abruptly (e.g. WebSocket drop), then the system automatically clears the orphaned lock and broadcasts a `disconnected` release event.

---

**Epic: The Vault - AI-Assisted Rules Query (New - Demo 3)**

##### US-VLT-07: Ask a Gameplay Question

**As a Standard Registered User, I want to ask a gameplay question in plain language, so that I can get a direct answer without manually searching through a rulebook.**

**Acceptance Criteria:**
- Given I am authenticated, when I submit a natural language gameplay question, then the system parses my query against the relevant `Ready`-status rulebook(s) using a RAG pipeline and returns a synthesised answer.
- Given the RAG pipeline retrieves relevant rulebook content, then the answer returned to me is grounded in that content rather than being generated without supporting context.
- Given no relevant content can be retrieved for my query, then the system informs me that it could not find an answer rather than returning an unsupported or fabricated response.
- Given I am an unauthenticated Guest, then the natural language query interface is not available to me.

---

### 4. Use Cases
The diagrams below present the high-level use cases per subsystem. Detailed use case specifications (actors, pre/postconditions, basic/alternative/exception flows) for every use case - including all Demo 2 and Demo 3 additions - are provided in Section 8 (Subsystems), directly beneath each subsystem's use case table. Actor participation reflects the permissions defined in Section 3.1: Guest Users have no interactive access to the Social & Events subsystem beyond registration/login, read-only access to Marketplace browsing, and read-only access to `Ready` rulebooks in The Vault.

#### 4.1 Social & Events - Use Case Diagram

![User Service Authentication Use Case Diagram](./diagrams/uc-auth.png)
![User Service Profile Management Use Case Diagram](./diagrams/uc-profile.png)
![User Service Socials Use Case Diagram](./diagrams/uc-social.png)
![User Service Events Use Case Diagram](./diagrams/uc-events.png)

#### 4.2 Marketplace - Use Case Diagram

![Marketplace Use Case Diagram](./diagrams/marketplace_use_case_diagram.png)

#### 4.3 The Vault - Use Case Diagram
![Vault Use Case Diagram](./diagrams/The_Vault_Use_Case_Diagram.png)

---

### 5. Functional Requirements

*High-level, encapsulated functionality only. Individual use cases are detailed in Section 8. Each requirement is assigned to the subsystem responsible for its implementation.*

**Social & Events Subsystem**

* **R1:** The system shall provide functionality for users to create and manage social groups.
* **R2:** The system shall provide functionality for users to organise, discover, and RSVP to board game events.
* **R5:** The system shall manage user accounts and profiles (CRUD).
* **R6:** The system shall manage groups and group membership (CRUD).
* **R7:** The system shall manage events and event participation records (CRUD).
* **R8:** The system shall manage the board game catalogue (CRUD).
* **R12:** The system shall authenticate users and manage session validity, including token revocation on logout.
* **R13:** The system shall integrate with Google Maps to display and geospatially index event locations.
* **R14:** The system shall integrate with BoardGameGeek (BGG) to retrieve board game metadata for the catalogue.
* **R16:** The system shall provide functionality for users to send, accept, reject, and remove direct peer-to-peer friend connections with other users.
* **R17:** The system shall manage friend connections and pending friend requests (CRUD).

**Marketplace Subsystem**

* **R3:** The system shall provide functionality for users to buy, sell, and rent board games through a peer-to-peer marketplace.
* **R9:** The system shall manage marketplace listings, including rental periods (CRUD).
* **R18:** The system shall proactively aggregate external retail listings for the platform's most popular board games via automated web scraping, and surface the cached results to users as a personalised "Web" tab within the Marketplace.

**The Vault Subsystem**

* **R4:** The system shall provide functionality for users to collaboratively create and edit digital rulebooks (The Vault).
* **R10:** The system shall manage rulebook metadata, including upload, ingestion status, and versioned content (CRUD).
* **R11:** The system shall support real-time collaborative editing of rulebooks, including edit locking, undo/redo, and full edit history.
* **R15:** The system shall expose a natural language interface for users to submit gameplay queries, utilising a Retrieval-Augmented Generation (RAG) pipeline to parse queried text against the uploaded rulebooks and return a synthesised answer.

*(Calculations category is not applicable for Demo 3 scope. R15 is the sole AI/Models requirement introduced in this milestone; broader generative AI functionality remains deferred per Section 2.3.)*

---

### 6. Non-Functional Requirements (Quantified Targets)

* **Reliability:** The system should achieve **99.9% uptime** and recover from critical failures within **5 minutes**.
* **Maintainability:** New features or bug fixes should be deployable within **2 hours**, and the codebase must maintain at least **80% automated test coverage**.
* **Usability:** A new user should be able to complete core tasks within **5 minutes** of first using the system, achieving at least **85% user satisfaction** during usability testing.
* **Availability:** The system must be available **24/7**, excluding scheduled maintenance periods which may not exceed **2 hours per month**.
* **Security:** All user passwords must be encrypted at rest (bcrypt, cost factor ≥ 10) and never stored or logged in plain text. All session tokens (JWTs) must be signed and validated on every request, and revoked tokens must be rejected via the `Token_Blacklist` within **1 second** of logout. **100% of state-changing Marketplace and Vault operations** (listing edit/delete, rulebook edit/lock) must verify resource ownership or lock possession server-side before executing. Internal service-to-service calls (e.g. the Spring Boot backend's re-embedding webhook to the FastAPI AI Gateway) must authenticate via a shared internal service token, distinct from user-facing JWTs, and must never be reachable using a user session token.
* **Performance & Reliability (RAG Query):** A natural language query is handled synchronously and must resolve well within typical gateway timeout limits. If the primary generation service is degraded, the system must automatically retry with exponential backoff before falling back to a locally hosted model, so that a query only fails outright if both the primary and fallback generation paths are unavailable.
* **Resource Efficiency (External Listing Scraper):** The external retailer scraper must share a single Playwright browser instance and `BrowserContext` across all three target sites (Takealot, Bobshop, ToysRUs) rather than opening a separate browser per site, to avoid over-utilising host resources. A per-search-term lock must prevent duplicate concurrent scrapes for the same term. The combined cache (MongoDB and in-memory) must not exceed **300** listings and must observe a **1-hour TTL** aligned to the hourly scrape schedule.

---

### 7. Domain Model
The high-level domain model below contains the exact union of all classes shown across the subsystem domain models in Section 8 - no class is added, omitted, or duplicated between this diagram and the subsystem-level diagrams. Where a class is referenced by more than one subsystem (e.g. `Board_Game`), it appears once here, owned by its home subsystem, and is shown only as a referenced/dashed dependency in the diagrams of subsystems that consume it.

*Pending update: this diagram still shows `Rulebook_Text` and `Chunk` as separate classes (`Chunk` embedded within `Rulebook_Text`). Per the correction in Section 8.3.2, these are now a single flattened `Rulebook_Text` collection (one document per chunk, including the `embedding` field). The diagram image should be regenerated to merge these into one class before this document is finalised.*

![System Domain Model](./diagrams/Improved_boardwise_domain_model.png)

---

### 8. Subsystems

*Use cases are numbered `U<subsystem>.<sequence>`, where `<subsystem>` is the subsystem number (1, 2, 3) and `<sequence>` is the use case's order within that subsystem (e.g. U1.1, U1.2, U2.1). This numbering is used consistently in each subsystem's use case diagram (Section 4) and in the Section 9 traceability matrix.*

#### Subsystem 8.1: Social & Events

##### 8.1.1 Use Cases

| ID | Use Case | Status |
| --- | --- | --- |
| U1.1 | Register an Account | Carried over |
| U1.2 | Log Into an Account | Carried over |
| U1.3 | Log Out of an Account | Carried over |
| U1.4 | Manage Profile | Carried over |
| U1.5 | View a Profile | Carried over |
| U1.6 | Manage Game Inventory «include» Add Game to Inventory, Remove Game from Inventory | **New - Demo 2** |
| U1.7 | Manage Groups | Carried over |
| U1.8 | Manage Events «include» Schedule an Event, Edit an Event | **New - Demo 2** |
| U1.9 | View Events | Carried over |
| U1.10 | RSVP to an Event | **New - Demo 2** |
| U1.11 | Manage Friend Connections «include» Send Friend Request, Accept/Reject Friend Request, View Friends List, Unfriend a User | **New - Demo 3** |

**U1.6: Manage Game Inventory**

| Field | Detail |
| --- | --- |
| **Use Case ID** | U1.6 |
| **Use Case Name** | Manage Game Inventory |
| **Actor(s)** | Standard Registered User |
| **Description** | A user searches the board game catalogue to add titles to their personal digital inventory or removes existing titles from their collection. |
| **Preconditions** | The user is authenticated. |
| **Postconditions** | The user's inventory is updated. If a new game is added that does not exist in the local database, it is fetched via the BoardGameGeek (BGG) integration and persisted to the local catalogue. |
| **Basic Flow (Add)** | 1. User navigates to their profile and selects to add a game. 2. User searches for a game title. 3. System retrieves matching results (utilizing the BGG integration if local catalogue results are insufficient). 4. User selects a game to add. 5. System persists the relationship between the `User` and the `Board_Game`. |
| **Basic Flow (Remove)** | 1. User views their inventory. 2. User selects the remove action on a specific board game. 3. System updates the inventory and removes the relationship. |

**U1.8: Manage Events**

| Field | Detail |
| --- | --- |
| **Use Case ID** | U1.8 |
| **Use Case Name** | Manage Events |
| **Actor(s)** | Standard Registered User (Event Organiser) |
| **Description** | A user schedules a new board game event or edits the details (date, time, location, visibility) of an existing event they have created. |
| **Preconditions** | The user is authenticated. For editing, the user must be the original creator of the event. |
| **Postconditions** | A new `Events` entity is persisted, or an existing one is updated. |
| **Basic Flow (Schedule)** | 1. User selects to create a new event. 2. User provides event details, utilizing the Google Maps integration to set and validate the event location. 3. User sets the event visibility (Public or Private) and selects a board game from the catalogue. 4. System validates inputs and creates the `Events` entity, automatically adding the creator as a participant. |
| **Basic Flow (Edit)** | 1. Organiser selects to edit their event. 2. System populates the form with existing `Events` data. 3. Organiser modifies details and submits. 4. System updates the entity and notifies existing participants of changes. |

**U1.10: RSVP to an Event**

| Field | Detail |
| --- | --- |
| **Use Case ID** | U1.10 |
| **Use Case Name** | RSVP to an Event |
| **Actor(s)** | Standard Registered User |
| **Description** | A user joins a visible board game event or withdraws an existing RSVP. |
| **Preconditions** | The user is authenticated. The event must exist and be visible to the user (either Public, or Private with a direct invite). |
| **Postconditions** | The `Event_Participants` record is updated to reflect the user's attendance status. |
| **Basic Flow** | 1. User navigates to an event details page. 2. User selects the action to "Join" (if not attending) or "Withdraw" (if already attending). 3. System validates the request and updates the `Event_Participants` linkage. 4. System updates the UI to reflect the new RSVP status. |

**U1.11: Manage Friend Connections**

| Field | Detail |
| --- | --- |
| **Use Case ID** | U1.11 |
| **Use Case Name** | Manage Friend Connections |
| **Actor(s)** | Standard Registered User |
| **Description** | A user sends, accepts, rejects, or removes a direct peer-to-peer friend connection with another user. |
| **Preconditions** | The user is authenticated. For accept/reject, a `Pending` `Friendship` request directed at the user must exist. For unfriend, an `Accepted` `Friendship` must exist between the two users. |
| **Postconditions** | A `Friendship` record is created with status `Pending`, updated to `Accepted`, or deleted, depending on the action taken. |
| **Basic Flow (Send Request)** | 1. User views another user's profile and selects "Add Friend". 2. System checks whether the target user has already sent the current user a pending request. 3. If a reciprocal pending request exists, the system instead presents the option to accept it (see Accept/Reject flow). 4. Otherwise, system creates a new `Friendship` record with status `Pending`. 5. The "Add Friend" option on the target's profile is replaced with a pending status indicator. |
| **Basic Flow (Accept/Reject)** | 1. User navigates to their friend requests/notifications page and views incoming pending requests. 2. User selects "Accept" or "Reject". 3a. Accept: System updates the `Friendship` status to `Accepted`; both users now appear on each other's friends list. 3b. Reject: System deletes the `Friendship` record; the requesting user is not added to the recipient's friends list. |
| **Basic Flow (View Friends List)** | 1. User navigates to their profile or friends page. 2. System retrieves all `Accepted` `Friendship` records involving the user. 3. System displays each friend's display name and profile picture, or an empty-state message if the user has no friends. |
| **Basic Flow (Unfriend)** | 1. User selects "Unfriend" from their friends list or a friend's profile. 2. System prompts the user to confirm the action. 3. On confirmation, system deletes the `Friendship` record, removing each user from the other's friends list. 4. The "Add Friend" option is restored on the (former) friend's profile. |
| **Exception Flow** | **1a.** Duplicate Pending Request: The user attempts to send a request to someone they have already sent a pending request to. The system takes no further action and continues to display the existing pending state. |

##### 8.1.2 Subsystem Domain Model

*Rule: A class must belong to one and only one subsystem. `Board_Game` is owned by Social & Events; Marketplace and The Vault reference it only via `ObjectId` (`gameId`/`games[]`/`ownedGames[]`) - no direct cross-service class ownership or join.*

Classes owned by this subsystem: `User`, `Preferences` *(embedded)*, `Friendship`, `Groups`, `Group_Membership`, `Events`, `Event_Participants`, `Token_Blacklist`, `Board_Game`.


* **User:** Core identity entity containing authentication and profile fields. *Constraints:* Email addresses must be unique. Passwords must be encrypted at rest.
* **Preferences (Embedded):** Stores the user's genre and mechanic preferences. *Constraints:* Governed by a `visibility` toggle (Public/Private).
* **Friendship:** Represents a direct peer-to-peer connection between two users. *Constraints:* Tracks a `status` of `Pending` (request sent, awaiting response) or `Accepted` (mutual connection). A rejected request or an unfriend action deletes the record rather than retaining a terminal status. A user cannot hold two simultaneous pending requests with the same counterpart - if a reciprocal pending request already exists, the system surfaces it for acceptance instead of creating a duplicate. Exposed via U1.11 (see Section 8.1.1).
* **Groups & Group_Membership:** Manages social hubs and user affiliations. *Constraints:* A `Group` must have at least one owner.
* **Events & Event_Participants:** Manages scheduled board game sessions and RSVPs. *Constraints:* `Events` visibility dictates who can create an `Event_Participants` record.
* **Token_Blacklist:** Manages session invalidation. *Constraints:* Stores revoked JWT signatures to prevent replay attacks post-logout.
* **Board_Game:** The central catalogue entity populated via the BoardGameGeek integration. *Constraints:* Acts as the source of truth for game metadata; referenced by ID in other subsystems.

---

#### Subsystem 8.2: Marketplace

##### 8.2.1 Use Cases

| ID | Use Case | Status |
| --- | --- | --- |
| U2.1 | Browse and Filter Community Listings | Carried over |
| U2.2 | View Full Listing Detail | Carried over |
| U2.3 | Create a Rental or Sale Listing | Carried over |
| U2.4 | Edit an Existing Listing | Carried over |
| U2.5 | Delete a Listing | Carried over |
| U2.6 | View External Retail Listings (Web Tab) | **New - Demo 3** |

**U2.6: View External Retail Listings (Web Tab)**

| Field | Detail |
| --- | --- |
| **Use Case ID** | U2.6 |
| **Use Case Name** | View External Retail Listings (Web Tab) |
| **Actor(s)** | Standard Registered User, System (background scraper) |
| **Description** | A scheduled background process scrapes three external retailers for the platform's currently most popular board games (ranked by inventory count) and caches matching listings; logged-in users view these cached listings under the Marketplace's "Web" tab. |
| **Preconditions** | The background scrape has no user-facing precondition and runs on a fixed hourly schedule. To view the tab, the user must be logged in as a Standard Registered User and navigate to the Marketplace. |
| **Postconditions** | Up to 300 validated external listings are cached (MongoDB and in-memory) with a 1-hour TTL; the Web tab reflects the current cache contents. |
| **Basic Flow (Background Popularity Scrape)** | 1. Every hour, a scheduled job computes a popularity ranking of games by counting `gameId` occurrences across all `User` inventories in the database. 2. One dedicated thread selects scrape targets from this ranking and dispatches search terms for the top-ranked games. 3. Two worker threads execute the searches via Playwright against Takealot, Bobshop, and ToysRUs, sharing a single Playwright instance and `BrowserContext` across all three sites to limit resource usage. 4. For each raw result, the system applies a Strategy-pattern-based matching strategy - currently Jaro-Winkler similarity between the listing title and the target game's name - to validate relevance; results scoring below a **65%** similarity threshold are discarded. 5. Valid listings are persisted to MongoDB for durability and mirrored into an in-memory `Collections.synchronizedMap` cache for fast reads, capped at a maximum of **300** cached listings with a **1-hour TTL** aligned to the scrape interval. |
| **Basic Flow (View Web Tab)** | 1. User navigates to the Marketplace and selects the "Web" tab. 2. System serves the current cached listings (in-memory cache preferred, MongoDB as the durable backing store) for the most popular games. 3. System displays each listing's retailer, title, price, and a link to the original external listing. |
| **Exception Flow** | **1a.** Duplicate Scrape In Flight: A scrape for a given search term is already running when another trigger for the same term occurs. A per-term lock prevents a second scrape from firing; the caller is served the currently cached result for that term instead. **2a.** Cache Expired, Refresh Pending: The cached TTL has elapsed but the next scheduled scrape has not yet completed. The system continues serving the last persisted MongoDB snapshot rather than blocking the user on a live scrape. |

##### 8.2.2 Subsystem Domain Model

Classes owned by this subsystem: `Listings`, `Rental_Period` *(embedded)*, `External_Listing`.

*Note: `Listings` does not reference `Board_Game` by ID; `gameTitle` and `genres` are freeform/denormalised fields captured at listing-creation time, decoupling the Marketplace Service from the User Service's game catalogue. `External_Listing`, by contrast, does reference `Board_Game` by ID, since it is machine-matched against a specific catalogue entry rather than freely entered by a user.*


* **Listings:** Represents a user-generated offer to rent or sell a board game. *Fields:* `listingType` (Rent/Sale), `price`, `status`, `gameTitle`, `genres`. *Constraints:* `gameTitle` and `genres` are denormalized at creation to decouple from the User Service catalogue. Price must be a positive value.
* **Rental_Period (Embedded):** Tracks availability for rental listings. *Constraints:* Start dates cannot be in the past; end dates must strictly follow start dates. Sale listings do not utilize this embedded document.
* **External_Listing:** A cached, system-generated listing scraped from an external retailer for a specific catalogue game. *Fields:* `retailer` (Takealot/Bobshop/ToysRUs), `gameId` (reference to `Board_Game`), `title`, `price`, `url`, `similarityScore`, `scrapedAt`. *Constraints:* Only persisted if its Jaro-Winkler similarity score against the target game's title is ≥ 65%. The collection is capped at 300 cached documents platform-wide and refreshed on a 1-hour TTL cycle; the in-memory `Collections.synchronizedMap` layer is a runtime mirror of this collection rather than a separate persisted class.

---

#### Subsystem 8.3: The Vault

##### 8.3.1 Use Cases

| ID | Use Case | Status |
| --- | --- | --- |
| U3.1 | Upload a Rulebook | **New - Demo 2** |
| U3.2 | Browse the Vault Library | Carried over |
| U3.3 | Read a Rulebook | Carried over |
| U3.4 | Edit a Rulebook *(Collaborative Editing)* | **New - Demo 2** |
| U3.5 | Undo and Redo Edits *(Collaborative Editing)* | **New - Demo 2** |
| U3.6 | View Rulebook Edit History | **New - Demo 2** |
| U3.7 | Lock Management and Release *(Collaborative Editing)* | **New - Demo 2** |
| U3.8 | Query Rulebooks via Natural Language (RAG) | **New - Demo 3** |

**U3.1: Upload a Rulebook**

| Field | Detail |
| --- | --- |
| **Use Case ID** | U3.1 |
| **Use Case Name** | Upload a Rulebook |
| **Actor(s)** | Standard Registered User |
| **Description** | A user uploads a PDF rulebook to The Vault, initiating the asynchronous ingestion and text-extraction pipeline. |
| **Preconditions** | The user is authenticated. The file is a valid PDF and does not exceed the 50 MB size limit. |
| **Postconditions** | An `Ingestion_Job` is initiated, the raw PDF is stored in Cloudflare R2, and metadata is persisted in MongoDB Atlas. |
| **Basic Flow** | 1. User navigates to The Vault and selects the option to upload. 2. User provides the PDF file, `game_name`, and selects the linked `game_id` from the catalogue. 3. User submits the upload. 4. The BFF streams the payload directly to the FastAPI service. 5. System immediately returns a `202 Accepted` status to the client and continues sanitization and extraction in the background. |
| **Exception Flow** | **3a.** File exceeds 50 MB, is an invalid format, or fails sanitization: The system rejects the upload with a `413`, `415`, or `422` error and displays a clear message to the user. |

**U3.4: Edit a Rulebook (Collaborative Editing)**

| Field | Detail |
| --- | --- |
| **Use Case ID** | U3.4 |
| **Use Case Name** | Edit a Rulebook |
| **Actor(s)** | Standard Registered User |
| **Description** | A user modifies, inserts, or deletes a text chunk in a rulebook using the real-time collaborative editor. |
| **Preconditions** | The user is authenticated and has successfully acquired an exclusive MRSW write lock for the target rulebook. |
| **Postconditions** | The `Chunk` data is updated, a record is appended to the `Edit_Event` ledger, the `Rulebook` version is incremented, the change is pushed to the undo stack, and the affected chunk is queued for re-embedding. |
| **Basic Flow** | 1. User makes an edit (modify, insert, or delete) to a rulebook chunk in the editor UI. 2. System validates the edit against the `expectedVersion` to prevent concurrent modification anomalies. 3. System persists the atomic change to the database and records it in the undo ledger. 4. System broadcasts the appropriate WebSocket event (`DELTA_COMMITTED`, `CHUNK_INSERTED`, or `CHUNK_DELETED`) to all active readers so their views update instantly. 5. The Spring Boot backend calls an internal webhook on the FastAPI AI Gateway, authenticated via a shared internal service token, instructing it to re-embed the affected chunk so subsequent RAG queries (U3.8) retrieve against up-to-date content. |
| **Exception Flow** | **2a.** Version Mismatch: The `expectedVersion` does not match the server's version. The system rejects the edit with a `409 Conflict` error. **5a.** Re-embedding Webhook Failure: The AI Gateway is unreachable or rejects the internal token. The edit itself is still committed and visible to readers; re-embedding is retried or logged for follow-up so the RAG index does not silently diverge from the edited content. |

**U3.5: Undo and Redo Edits (Collaborative Editing)**

| Field | Detail |
| --- | --- |
| **Use Case ID** | U3.5 |
| **Use Case Name** | Undo and Redo Edits |
| **Actor(s)** | Standard Registered User |
| **Description** | A user reverts their most recent edit or reapplies an undone edit without manually re-typing or deleting text. |
| **Preconditions** | The user is authenticated and holds the active MRSW write lock. The undo (or redo) stack for their current session is not empty. |
| **Postconditions** | An inverse operation is applied to the `Chunk` data, a compensating entry is added to the `Edit_Event` ledger, the `Rulebook` version is incremented, and the affected chunk is queued for re-embedding via the same internal webhook used for direct edits (U3.4). |
| **Basic Flow** | 1. User triggers an "Undo" or "Redo" action in the editor. 2. System pops the target operation from the respective stack. 3. System applies the exact inverse operation (e.g., reverting a deletion by re-inserting the chunk). 4. System writes a compensating ledger entry to maintain immutability. 5. System broadcasts the corresponding WebSocket event reflecting the inverse change. |
| **Exception Flow** | **1a.** Stack Empty: If the user attempts to undo/redo when the stack is empty, the system ignores or rejects the request. |

**U3.6: View Rulebook Edit History**

| Field | Detail |
| --- | --- |
| **Use Case ID** | U3.6 |
| **Use Case Name** | View Rulebook Edit History |
| **Actor(s)** | Standard Registered User |
| **Description** | A user views the full, chronological version history of a rulebook to audit changes. |
| **Preconditions** | The user is authenticated. |
| **Postconditions** | The complete history of the rulebook is displayed; no state is modified. |
| **Basic Flow** | 1. User selects to view the edit history for a specific rulebook. 2. System retrieves the immutable `Edit_Event` ledger for that rulebook from the database. 3. System renders an ordered list detailing the total number of edits. 4. For each event, the UI displays the `editType`, target `chunkId`, resolving editor's username, the before/after text content, and the resulting `versionPostEdit`. |

**U3.7: Lock Management and Release (Collaborative Editing)**

| Field | Detail |
| --- | --- |
| **Use Case ID** | U3.7 |
| **Use Case Name** | Lock Management and Release |
| **Actor(s)** | Standard Registered User, System |
| **Description** | The system manages the release of MRSW write locks either voluntarily by the user or automatically upon disconnection. |
| **Preconditions** | The user holds at least one active write lock. |
| **Postconditions** | The write lock is cleared from the database, allowing other users to acquire it. |
| **Basic Flow (Voluntary)** | 1. User finishes editing and explicitly selects the action to release the lock (or triggers a "release all" command). 2. System clears the lock association in the database. 3. System broadcasts a lock released event with the reason `voluntary` via WebSocket to notify waiting users. |
| **Basic Flow (Automatic)** | 1. A user holding a lock abruptly disconnects (e.g., WebSocket session drops). 2. System detects the disconnection and automatically triggers a "release all" flow to clear orphaned locks. 3. System broadcasts a lock released event with the reason `disconnected`. |

**U3.8: Query Rulebooks via Natural Language (RAG)**

| Field | Detail |
| --- | --- |
| **Use Case ID** | U3.8 |
| **Use Case Name** | Query Rulebooks via Natural Language (RAG) |
| **Actor(s)** | Standard Registered User |
| **Description** | A user selects a specific `Ready`-status rulebook and submits a gameplay question about it in natural language. The system retrieves the most relevant chunks from that rulebook via vector search and cross-encoder re-ranking, synthesises a grounded answer using an LLM, and returns the answer together with structured citations back to the source chunks. |
| **Preconditions** | The user is authenticated. The target rulebook exists and has `Ready` ingestion status. The submitted query is between 3 and 500 characters. |
| **Postconditions** | A synthesised answer and its supporting citations are returned to the user. The query is not persisted; no rulebook, chunk, or ledger state is modified. |
| **Basic Flow** | 1. User selects a rulebook and submits a gameplay question. 2. System embeds the query using the Nomic Embed v1.5 model and truncates it to the same 256-dimension Matryoshka representation used for stored chunk embeddings. 3. System executes a MongoDB Atlas `$vectorSearch` against the `Rulebook_Text` collection, pre-filtered to the selected `rulebookId`, retrieving up to 15 candidate chunks. 4. System re-ranks the candidates with a cross-encoder model and selects the top 3 most relevant chunks as grounding context. 5. System sends the query and the retrieved context to the generation model under a strict grounding system prompt, holding the request open synchronously until a response is returned. 6. System returns the synthesised answer to the user along with a structured citation list (source `chunkId`, chunk `index`, matched `content`, and `relevanceScore`) so the user can jump to the exact source passage. |
| **Exception Flow** | **1a.** Invalid Query Length: The submitted query is shorter than 3 or longer than 500 characters. The system rejects the request with a validation error before any retrieval occurs. **3a.** No Relevant Content Retrieved: Retrieval returns no chunks above the relevance threshold. The system returns a fixed response indicating the answer could not be found in the provided rulebook, rather than allowing the model to speculate. **5a.** Primary Generation Service Degraded: The primary LLM service returns a rate-limit or cold-start error. The system retries with exponential backoff (up to 3 attempts) before falling back to a locally hosted, quantised LLM running in the AI Gateway container. **5b.** Total Generation Failure: Both the primary service and the local fallback model fail. The system returns a `503` error indicating the AI service is currently unavailable. |

*Note: chunk embeddings are kept current via the internal re-embedding webhook described in U3.4/U3.5 - a committed edit (including undo/redo) triggers re-embedding of the affected chunk shortly after the edit is persisted, so this pipeline retrieves against the chunk content as of its last commit rather than a stale, pre-edit embedding.*

##### 8.3.2 Subsystem Domain Model

*Note: the `Rulebook_Text` schema is a flattened collection - one document represents one chunk - rather than a `Chunk` array embedded within a parent document. This corrects the Demo 2 description of this class; the class list below and Section 7's diagram should be read accordingly.*

Classes owned by this subsystem: `Rulebook`, `Rulebook_Text`, `Edit_Event`, `Ingestion_Job`.


* **Ingestion_Job:** Tracks the asynchronous processing status of uploaded PDFs. *Constraints:* Statuses include Pending, Processing, Ready, or Failed.
* **Rulebook:** The metadata envelope for the digital manual. *Constraints:* Must maintain a strict version counter to validate incoming edits.
* **Rulebook_Text:** A flattened collection in which each document represents a single chunk of a rulebook's content, keyed by its own `chunkId` and linked to its parent via `rulebookId`. *Fields:* `chunkId`, `rulebookId`, `index`, `content`, `embedding`, `charCount`, `metadata`, `createdAt`, `updatedAt`. *Constraints:* Chunks must maintain a strict `index` order within a given `rulebookId`. The `embedding` field holds a 256-dimension Matryoshka-truncated vector (Nomic Embed v1.5), indexed via MongoDB Atlas Vector Search - making this the single collection consumed by both the collaborative editor (R11) and the RAG retrieval pipeline (R15). A committed edit triggers an internal webhook from the Spring Boot backend to the FastAPI AI Gateway to refresh this field, keeping retrieval aligned with the latest edited content.
* **Edit_Event:** The event sourcing ledger tracking all collaborative changes. *Constraints:* Strictly immutable. Every atomic commit (including Undo/Redo) appends a new record detailing the `editType`, `chunkId` (the target `Rulebook_Text` document's ID), and resulting version.

---

### 9. Traceability Matrix

| Use Case | R1 | R2 | R3 | R4 | R5 | R6 | R7 | R8 | R9 | R10 | R11 | R12 | R13 | R14 | R15 | R16 | R17 | R18 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| U1.1 Register an Account |  |  |  |  |  |  |  |  |  |  |  | X |  |  |  |  |  |  |
| U1.2 Log Into an Account |  |  |  |  |  |  |  |  |  |  |  | X |  |  |  |  |  |  |
| U1.3 Log Out of an Account |  |  |  |  |  |  |  |  |  |  |  | X |  |  |  |  |  |  |
| U1.4 Manage Profile |  |  |  |  | X |  |  |  |  |  |  |  |  |  |  |  |  |  |
| U1.5 View a Profile |  |  |  |  | X |  |  |  |  |  |  |  |  |  |  |  |  |  |
| U1.6 Manage Game Inventory |  |  |  |  |  |  |  | X |  |  |  |  |  | X |  |  |  |  |
| U1.7 Manage Groups | X |  |  |  |  | X |  |  |  |  |  |  |  |  |  |  |  |  |
| U1.8 Manage Events |  | X |  |  |  |  | X |  |  |  |  |  | X |  |  |  |  |  |
| U1.9 View Events |  | X |  |  |  |  | X |  |  |  |  |  |  |  |  |  |  |  |
| U1.10 RSVP to an Event |  | X |  |  |  |  | X |  |  |  |  |  |  |  |  |  |  |  |
| U1.11 Manage Friend Connections |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  | X | X |  |
| U2.1 Browse and Filter Listings |  |  | X |  |  |  |  |  | X |  |  |  |  |  |  |  |  |  |
| U2.2 View Full Listing Detail |  |  | X |  |  |  |  |  | X |  |  |  |  |  |  |  |  |  |
| U2.3 Create a Rental or Sale Listing |  |  | X |  |  |  |  |  | X |  |  |  |  |  |  |  |  |  |
| U2.4 Edit an Existing Listing |  |  | X |  |  |  |  |  | X |  |  |  |  |  |  |  |  |  |
| U2.5 Delete a Listing |  |  | X |  |  |  |  |  | X |  |  |  |  |  |  |  |  |  |
| U2.6 View External Retail Listings (Web Tab) |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  | X |
| U3.1 Upload a Rulebook |  |  |  | X |  |  |  | X |  | X |  |  |  |  |  |  |  |  |
| U3.2 Browse the Vault Library |  |  |  | X |  |  |  |  |  | X |  |  |  |  |  |  |  |  |
| U3.3 Read a Rulebook |  |  |  | X |  |  |  |  |  | X |  |  |  |  |  |  |  |  |
| U3.4 Edit a Rulebook |  |  |  | X |  |  |  |  |  |  | X |  |  |  |  |  |  |  |
| U3.5 Undo and Redo Edits |  |  |  | X |  |  |  |  |  |  | X |  |  |  |  |  |  |  |
| U3.6 View Rulebook Edit History |  |  |  | X |  |  |  |  |  |  | X |  |  |  |  |  |  |  |
| U3.7 Lock Management and Release |  |  |  | X |  |  |  |  |  |  | X |  |  |  |  |  |  |  |
| U3.8 Query Rulebooks via Natural Language (RAG) |  |  |  | X |  |  |  |  |  | X |  |  |  |  | X |  |  |  |

*Note: R8 (board game catalogue management) is satisfied indirectly - via U1.6 (adding a game to inventory may create/reference a catalogue entry) and U3.1 (rulebook upload references an existing `gameId`) - rather than via a standalone catalogue-browsing use case. This reflects the actual data flow confirmed in the ERD.*