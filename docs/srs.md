# Boardwise
## Software Requirements and Design Specifications

**Department of Computer Science**  
**Faculty of Engineering, Built Environment & IT**  
**University of Pretoria**  
**COS301 — Software Engineering**

---

**Item:** Mini Project 2026 — Phase 1  
**Team Name:** Works On My Machine

**Team Members:**

| Name | Surname | Student Number | % Contribution |
|---|---|---|---|
|Hayley\* |Booysen |u24868346 | --|
|Bandile |Mnyandu |u24675394 | --|
|Karabo |Nkomo |u24865169 |-- |
|Palesa |Nkosi |u22664638 |-- |
|Njabulo |Mathonsi |u24676412 |-- |

*\* — indicates team leader*

---

## Table of Contents

1. [Introduction](#1-introduction)
2. [Project Owner](#2-project-owner)
3. [Project Vision and Objectives](#3-project-vision-and-objectives)
4. [Functional Requirements](#4-functional-requirements)
5. [Non-Functional Requirements](#5-non-functional-requirements)
6. [Technology and Project Constraints](#6-technology-and-project-constraints)
7. [User Characteristics](#7-user-characteristics)
8. [System Domain Model](#8-system-domain-model)
9. [Subsystems](#9-subsystems)
   - [9.1 User Service (Including Community)](#91-user-service-including-community)
   - [9.2 Marketplace Service](#92-marketplace-service)
   - [9.3 Shared Library — The Vault](#93-shared-library--the-vault)
10. [API Service Contracts](#10-api-service-contracts)
    - [10.1 User Service API Contracts](#101-user-service-api-contracts)
    - [10.2 Marketplace API Contracts](#102-marketplace-api-contracts)
    - [10.3 The Vault API Contracts](#103-the-vault-api-contracts)
11. [Traceability Matrix](#11-traceability-matrix)
12. [Architectural Requirements](#12-architectural-requirements)
    - [12.1 Overall Software Architecture](#121-overall-software-architecture)
    - [12.2 Architectural Quality Requirements](#122-architectural-quality-requirements)
    - [12.3 Architectural Constraints](#123-architectural-constraints)
    - [12.4 Architectural Components](#124-architectural-components)
    - [12.5 Summary](#125-summary)

---

## 1. Introduction

Boardwise is a comprehensive platform designed to digitise and expand the board game experience for enthusiasts, particularly within the South African market. The South African board gaming community remains largely offline and fragmented; local stores host gaming days, but enthusiasts are often tied to specific retail locations to find community. Boardwise centralises this ecosystem, providing a store-agnostic platform where the community can connect, rent, and organise events independently.

The platform addresses key community problems: enabling peer-to-peer (P2P) transactions (rental and sale) without dependence on a single retailer, fostering community formation through groups and events, and democratising access to board game knowledge through a collaboratively maintained shared library of rulebooks.

**Scope Note:** This document reflects the requirements in scope for **Sprint 1 (Demo 1)**. The focus is on delivering a functional MVP with core CRUD capabilities across all system domains. The following features are explicitly deferred to future sprints and are **not covered** by this document: personalised recommendation systems, RAG-based natural language query interface, PWA installability, Generative Game Architect (AI Innovation), Setup Wizard, app theming, price comparison, and automated retrieval of rulebooks from the internet.

---

## 2. Project Owner

Name: Saskia Steyn  
Email: saskia.steyn@labs.epiuse.com

---

## 3. Project Vision and Objectives

Boardwise is a comprehensive digital ecosystem for board game enthusiasts that **optimises collection management and community engagement**. The platform features a peer-to-peer marketplace, a community events hub, and a shared digital vault for storing and collaboratively maintaining board game rulebooks.

Designed to foster local gaming networks, the application facilitates peer-to-peer game rentals and a thematic event-hosting system for organised play. By consolidating discovery, logistics, and social coordination into a single interface, it streamlines the hobby for players and collectors alike.

The objectives of the system are to:

- Provide a digital home for board game collections and community interaction in South Africa.
- Enable peer-to-peer transactions (rental and sale) without dependence on a single retailer.
- Foster community formation through groups, events, and social connections.
- Democratise access to board game knowledge through a collaboratively maintained shared library of rulebooks.

---

## 4. Functional Requirements

### 4.1 User Profile & Social Domain

- **FR1.1:** The system must allow users to create, read, update, and delete (CRUD) personal profiles.
- **FR1.2:** The system must enable users to maintain a digital inventory of owned board games.
- **FR1.3:** The system must record and store user genre and game mechanic preferences.
- **FR1.4:** The system must facilitate sending, accepting, and rejecting friend requests to form peer groups.

### 4.2 Marketplace Domain

### 4.3 Community & Events Domain

- **FR3.1:** The system must allow users to schedule gaming events, defining parameters such as date, time, game, and visibility (Public or Private).
- **FR3.2:** The system must process event RSVPs, allowing users to join or decline event invitations.

### 4.4 Shared Library Domain (The Vault)

### 4.5 UI & Usability

- **FR5.1:** The system must provide contextual help text or tooltips for complex interactions (e.g., uploading a rulebook to The Vault or setting up a P2P rental listing).

---

## 5. Non-Functional Requirements

### 5.1 Performance & Hardware Compatibility

- **NFR1.1:** The Vue.js Shared Library interface must process collaborative rulebook updates dynamically without requiring a full page reload.
- **NFR1.2:** The application frontend must be optimised to achieve fast initial load times and maintain smooth UI performance across mid-range mobile and desktop devices.
- **NFR1.3:** The system must handle real-time state updates gracefully under stable network conditions, with fallback states (e.g., loading skeletons) for slower connections.

### 5.2 Usability & Accessibility

- **NFR2.1:** The user interface must be fully responsive, scaling automatically to accommodate multiple screen sizes (mobile, tablet, and desktop).
- **NFR2.2:** The system must be accessible to users with disabilities, adhering to WCAG 2.1 Level AA guidelines, including sufficient contrast ratios, screen-reader compatibility, and full keyboard navigation support.

### 5.3 Security & Reliability

- **NFR3.1:** All P2P rental contracts and associated booking data processed by the Spring Boot backend must be ACID compliant.
- **NFR3.2:** The Shared Library must implement Multi-Reader Single-Writer (MRSW) versioning to prevent data corruption when multiple users attempt to edit a rulebook simultaneously.
- **NFR3.3:** The application must adhere to data privacy best practices, including encrypting passwords at rest and utilising secure JWTs for session management.

---

## 6. Technology and Project Constraints

- **CON1 (Licensing):** The entire system codebase must be released and maintained under an Open Source licence.
- **CON2 (Infrastructure):** All backend services (Spring Boot core, Node.js BFF, FastAPI AI Gateway, and MongoDB database) must be hosted exclusively on free cloud platform services or within standard free-tier limits.
- **CON3 (Target Hardware):** The application architecture must remain lightweight enough that both client-side rendering (Vue.js) and server-side processing do not demand high-end hardware, targeting optimisation for mid-range device groups.

---

## 7. User Characteristics

The Boardwise platform serves several distinct user types, each interacting with the system in different ways:

**Registered User (General):** Any authenticated user of the platform. They have access to all core features including profile management, game inventory, social features, marketplace browsing, event participation, and the Shared Library. They can take on more specific roles within the Vault.

**Guest User:** An unauthenticated visitor. Limited to browsing public marketplace listings and viewing basic game information. Cannot create listings, access the Vault, or interact with community features.

**Contributor (Vault):** A registered user who uploads PDF rulebooks to the Shared Library. Requires a valid authenticated session. Responsible for providing quality, correctly attributed rulebook documents.

**Collaborator (Vault):** A registered user who edits and maintains rulebook text in the Shared Library's collaborative editor. Must acquire an exclusive write lock before editing. Responsible for correcting errors and applying official publisher errata.

**Event Organiser:** A registered user who creates and manages gaming events. Controls event visibility (Public or Private), date, game, and attendee management.

**Listing Owner (Marketplace):** A registered user who creates, edits, or removes their own rental or sale listings in the marketplace. Responsible for the accuracy and availability of their listings.

---

## 8. System Domain Model

The system domain model illustrates the core entities across all subsystems and their relationships. The system is divided into three primary logical subsystems: the User Service (encompassing profiles, social features, and events), the Marketplace Service, and the Shared Library (The Vault).

The User entity is central to the entire system, forming the primary actor in all interactions across all domains. Each subsystem maintains its own set of domain classes, with cross-subsystem interactions mediated through service API calls rather than direct class coupling.

![System Domain Model](./diagrams/domain-model-system.png)

---

## 9. Subsystems

### 9.1 User Service (Including Community)

The User Service is responsible for managing all user-centric data and interactions on the Boardwise platform. This subsystem encompasses authentication (registration, login, logout), profile management (create, update, delete), game inventory management, user preference management, social features (friend requests and groups), and community features (events and RSVPs). It serves as the identity and social backbone of the platform that all other subsystems depend upon for user context.

#### 9.1.1 Domain Model

The User Service domain model centres on the `User` class, which holds core identity and profile attributes. The `User` maintains associations with `Boardgame` (ownership), `FriendRequest` (social connectivity), `Preferences` (genre and mechanic preferences), `Event` (creation and attendance), and `Group` (membership). The `FriendRequest` entity tracks the sender, receiver, and status of a connection request.

![User Service Domain Model](./diagrams/User_service_domain_model.drawio.png)

#### 9.1.2 User Stories

---

**Epic: Authentication**

##### US-AUTH-01: Register an Account

**As a user, I want to register an account, so that I can access the Boardwise platform and its features.**

**Acceptance Criteria:**
- Given I am on the registration page, when I provide a valid username, email address, and password and submit the form, then a new account is created and I am redirected to my profile setup page.
- Given I am on the registration page, when I submit the form with an email address that is already registered, then the system displays an error message informing me that the email is already in use.
- Given I am on the registration page, when I submit the form with any required field left empty, then the system displays a validation error and does not create an account.
- Given I have successfully registered, then my password is stored in an encrypted format and is never stored in plain text.

---

##### US-AUTH-02: Log Into an Account

**As a user, I want to log into my account, so that I can access my personalised profile and platform features.**

**Acceptance Criteria:**
- Given I am on the login page, when I provide a valid registered email and correct password, then I am authenticated and redirected to my home feed.
- Given I am on the login page, when I provide an incorrect password or unregistered email, then the system displays a generic error message and does not grant access.
- Given I have successfully logged in, then a secure JWT is issued and used to manage my session.
- Given I am on the login page, when I leave any required field empty and submit, then the system displays a validation error.

---

##### US-AUTH-03: Log Out of an Account

**As a user, I want to log out of my account, so that I can ensure my account is secure when I am done using the platform.**

**Acceptance Criteria:**
- Given I am logged in, when I select the logout option, then my session is terminated, my JWT is invalidated, and I am redirected to the login page.
- Given I have logged out, when I attempt to navigate to a protected page, then I am redirected to the login page and access is denied.

---

**Epic: Profile Management**

##### US-PROF-01: Create a Profile

**As a user, I want to create a personal profile, so that other users can identify me and I can personalise my experience on the platform.**

**Acceptance Criteria:**
- Given I have just registered, when I am directed to the profile setup page, then I can enter a display name, bio, and profile picture.
- Given I am setting up my profile, when I submit the form with at least a display name, then my profile is created and saved successfully.
- Given I am setting up my profile, when I submit the form without a display name, then the system displays a validation error and does not save the profile.

---

##### US-PROF-02: View a Profile

**As a user, I want to view my profile and the profiles of other users, so that I can see their information, game collections, and gaming preferences.**

**Acceptance Criteria:**
- Given I am logged in, when I navigate to my profile page, then I can see my display name, bio, profile picture, game inventory, and preferred genres and mechanics.
- Given I am logged in, when I navigate to another user's profile page, then I can see their display name, bio, profile picture, game inventory, and preferred genres and mechanics in a read-only view.
- Given a profile does not exist, when I navigate to that profile's URL, then the system displays a not-found message.

---

##### US-PROF-03: Update a Profile

**As a user, I want to update my profile information, so that I can keep my details accurate and up to date.**

**Acceptance Criteria:**
- Given I am on my profile page, when I select the edit option, then I can modify my display name, bio, and profile picture.
- Given I am editing my profile, when I save my changes with a valid display name, then the updated information is saved and reflected on my profile immediately.
- Given I am editing my profile, when I attempt to save with the display name field empty, then the system displays a validation error and does not save the changes.

---

##### US-PROF-04: Delete a Profile

**As a user, I want to delete my account and profile, so that I can remove my personal data from the platform.**

**Acceptance Criteria:**
- Given I am on my account settings page, when I select the delete account option, then the system prompts me to confirm the action before proceeding.
- Given I have confirmed the deletion, when the system processes the request, then my profile, game inventory, preferences, and associated data are permanently removed.
- Given my account has been deleted, when I attempt to log in with my previous credentials, then the system displays an error and denies access.

---

**Epic: Game Inventory**

##### US-INV-01: Add a Game to My Inventory

**As a user, I want to add board games to my digital inventory, so that I can keep track of the games I own.**

**Acceptance Criteria:**
- Given I am on my profile or inventory page, when I search for a board game and select it, then it is added to my game inventory.
- Given I attempt to add a game that already exists in my inventory, then the system displays a message informing me the game is already in my collection and does not create a duplicate entry.

---

##### US-INV-02: View a Game Inventory

**As a user, I want to view my own game inventory and the inventories of other users, so that I can see what board games are owned across the platform.**

**Acceptance Criteria:**
- Given I am on my profile page, when I navigate to my inventory, then I can see a list of all board games I have added, with options to manage them.
- Given I am viewing another user's profile, when I navigate to their inventory section, then I can see a read-only list of the board games they own, with no ability to modify their collection.
- Given a user's inventory is empty, when I navigate to their inventory section, then the system displays a message indicating no games have been added yet.

---

##### US-INV-03: Remove a Game from My Inventory

**As a user, I want to remove a board game from my inventory, so that I can keep my collection accurate if I no longer own a game.**

**Acceptance Criteria:**
- Given I am viewing my inventory, when I select the remove option on a game, then the system prompts me to confirm the action.
- Given I have confirmed the removal, then the game is removed from my inventory and no longer appears in my collection.

---

**Epic: Preferences**

##### US-PREF-01: Set Game Preferences

**As a user, I want to set my board game genre and mechanic preferences, so that other users can see what I enjoy.**

**Acceptance Criteria:**
- Given I am on my profile or settings page, when I navigate to preferences, then I can select from a list of available genres and game mechanics.
- Given I have selected my preferences, when I save them, then they are stored and displayed on my profile in a read-only view for other users.
- Given I have not set any preferences, when I visit the preferences page, then the system displays all options in an unselected state.
- Given another user is viewing my profile, when they view my preferences, then they can see my selected genres and mechanics but cannot modify them.

---

##### US-PREF-02: Update Game Preferences

**As a user, I want to update my genre and mechanic preferences, so that my profile reflects changes in my gaming interests over time.**

**Acceptance Criteria:**
- Given I am on the preferences page, when I modify my selected genres or mechanics and save, then the updated preferences are stored and reflected immediately.

---

**Epic: Social — Friends**

##### US-SOC-01: Send a Friend Request

**As a user, I want to send a friend request to another user, so that I can connect with them on the platform.**

**Acceptance Criteria:**
- Given I am viewing another user's profile, when I select the add friend option, then a friend request is sent to that user.
- Given I have already sent a friend request to a user, when I view their profile, then the add friend option is replaced with a pending status indicator.
- Given the other user has already sent me a friend request, when I attempt to send one to them, then the system instead presents me with the option to accept their existing request.

---

##### US-SOC-02: Accept or Reject a Friend Request

**As a user, I want to accept or reject incoming friend requests, so that I can control who is in my friend network.**

**Acceptance Criteria:**
- Given I have received a friend request, when I navigate to my notifications or friend requests page, then I can see the request with options to accept or reject.
- Given I accept a friend request, then both users are added to each other's friends list.
- Given I reject a friend request, then the request is removed and the requesting user is not added to my friends list.

---

##### US-SOC-03: View Friends List

**As a user, I want to view my friends list, so that I can see all the users I am connected with.**

**Acceptance Criteria:**
- Given I am on my profile or friends page, when I navigate to my friends list, then I can see all users I am currently friends with, including their display names and profile pictures.
- Given I have no friends added, when I navigate to my friends list, then the system displays a message indicating my friends list is empty.

---

##### US-SOC-04: Unfriend a User

**As a user, I want to remove a user from my friends list, so that I can manage my social connections on the platform.**

**Acceptance Criteria:**
- Given I am viewing my friends list or a friend's profile, when I select the unfriend option, then the system prompts me to confirm the action.
- Given I confirm the unfriend action, then the user is removed from my friends list and I am removed from theirs.
- Given I have unfriended a user, when I view their profile, then the add friend option is displayed again.

---

**Epic: Social — Groups**

##### US-GRP-01: Create a Group

**As a user, I want to create a group, so that I can organise a dedicated space for a specific set of users to connect.**

**Acceptance Criteria:**
- Given I am on the groups page, when I select the create group option and provide a group name, then a new group is created with me as the owner.
- Given I am creating a group, when I submit the form without a group name, then the system displays a validation error and does not create the group.
- Given I have created a group, then I am automatically added as a member of that group.

---

##### US-GRP-02: Join a Group

**As a user, I want to join an existing group, so that I can connect with other users who share my board gaming interests.**

**Acceptance Criteria:**
- Given I am browsing or searching groups, when I select the join option on a public group, then I am added as a member of that group.
- Given I am already a member of a group, when I view that group, then the join option is not displayed.

---

##### US-GRP-03: View a Group

**As a user, I want to view a group's details and members, so that I can see who is part of the group.**

**Acceptance Criteria:**
- Given I am a member of a group, when I navigate to the group's page, then I can see the group name, description, and a list of its members.
- Given I am not a member of a public group, when I navigate to the group's page, then I can see the group's basic details but am prompted to join.

---

**Epic: Community — Events**

##### US-EVT-01: Schedule an Event

**As a user, I want to schedule a gaming event, so that I can organise a session for other users to join.**

**Acceptance Criteria:**
- Given I am on the events page, when I select the create event option and provide a name, date, time, and game, then a new event is created and saved.
- Given I am creating an event, when I set the visibility to Private, then only users I invite or friends can see and join the event.
- Given I am creating an event, when I set the visibility to Public, then any user on the platform can see and join the event.
- Given I submit the event creation form with any required field missing, then the system displays a validation error and does not create the event.

---

##### US-EVT-02: View Events

**As a user, I want to view available gaming events, so that I can find sessions to join.**

**Acceptance Criteria:**
- Given I am on the events page, when I browse the events list, then I can see all public events and any private events I have been invited to.
- Given there are no events available, when I navigate to the events page, then the system displays a message indicating no events are currently scheduled.

---

##### US-EVT-03: Update an Event

**As a user, I want to update the details of an event I have created, so that I can keep the event information accurate.**

**Acceptance Criteria:**
- Given I am the creator of an event, when I navigate to the event and select the edit option, then I can modify the event name, date, time, game, and visibility.
- Given I have made changes to the event, when I save, then the updated details are reflected immediately for all users who can view the event.
- Given I am not the creator of an event, when I view that event, then no edit option is presented to me.

---

##### US-EVT-04: RSVP to an Event

**As a user, I want to RSVP to a gaming event, so that the event organiser knows I plan to attend.**

**Acceptance Criteria:**
- Given I am viewing a public event or a private event I have been invited to, when I select the join option, then my RSVP is recorded and I am added to the event's attendee list.
- Given I have already joined an event, when I view that event, then the join option is replaced with an option to decline or withdraw my RSVP.
- Given I select the decline option on an event I have joined, then I am removed from the attendee list.

---

### 9.2 Marketplace Service

### 9.3 Shared Library - The Vault Service

---

## 10. API Service Contracts

### 10.1 User Service API Contracts

### 10.2 Marketplace Service API Contracts

### 10.3 The Vault Service API Contracts

---

## 11. Traceability Matrix

---

## 12. Architectural Requirements

### 12.1 Overall Software Architecture

#### 12.1.1 High-Level Architecture

#### 12.1.2 Communication Patterns

#### 12.1.3 Architectural Patterns

---

### 12.2 Architectural Quality Requirements

#### 12.2.1 Maintainability

#### 12.2.2 Scalability

#### 12.2.3 Flexibility

#### 12.2.4 Performance

#### 12.2.5 Security

#### 12.2.6 Reliability

#### 12.2.7 Usability

#### 12.2.8 Testability

---

### 12.3 Architectural Constraints

---

### 12.4 Architectural Components

#### 12.4.1 User Service

---

#### 12.4.2 Marketplace Service

---

#### 12.4.3 Shared Library — The Vault

---

### 12.5 Summary