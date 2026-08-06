<div align="center">

<img src="./docs/assets/boardwise-logo.svg" alt="Boardwise" width="420"/>

<img src="./docs/assets/slogan.svg" alt="Boardwise" width="420"/>


## Team

Boardwise is a comprehensive digital ecosystem for board game enthusiasts that consolidates collection management, a peer-to-peer marketplace, community events, and a collaboratively maintained shared rulebook library into a single platform.

---

![Vue](https://img.shields.io/badge/Vue.js-Frontend-42b883?style=for-the-badge&logo=vue.js&logoColor=white)
![Nuxt](https://img.shields.io/badge/Nuxt-BFF-00DC82?style=for-the-badge&logo=nuxt.js&logoColor=white)
![SpringBoot](https://img.shields.io/badge/Spring_Boot-Services-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![FastAPI](https://img.shields.io/badge/FastAPI-AI_Gateway-009688?style=for-the-badge&logo=fastapi&logoColor=white)
![MongoDB](https://img.shields.io/badge/MongoDB_Atlas-Database-47A248?style=for-the-badge&logo=mongodb&logoColor=white)

---

## Project Badges

![Build](https://img.shields.io/badge/build-passing-brightgreen?style=flat-square&logo=githubactions&logoColor=white)
![Coverage](https://img.shields.io/badge/coverage-80%25-success?style=flat-square&logo=codecov&logoColor=white)
![Requirements](https://img.shields.io/badge/requirements-SRS_v4-blue?style=flat-square&logo=readme&logoColor=white)
![Issues](https://img.shields.io/badge/issues-tracked-blueviolet?style=flat-square&logo=github&logoColor=white)
![Monitoring](https://img.shields.io/badge/uptime-monitored-orange?style=flat-square&logo=uptimerobot&logoColor=white)
![License](https://img.shields.io/badge/license-MIT-lightgrey?style=flat-square&logo=opensourceinitiative&logoColor=white)

---

# SHARED LIBRARY • MARKETPLACE • COMMUNITY

*A COS 301 Capstone Project — University of Pretoria*
*Team: Works On My Machine™ · Client: EPI-USE Labs*

</div>

---

# Project Overview

Boardwise digitises and expands the tabletop gaming experience for the South African board gaming community, which remains largely offline and fragmented. The platform is a store-agnostic ecosystem where enthusiasts can connect, rent and sell games peer-to-peer, organise events, and collaboratively maintain a shared library of digitised rulebooks.

The system is built around three core domains, each owned by a dedicated backend service: a **User & Community Service**, a **Marketplace Service**, and a **Shared Library (The Vault)** with an AI ingestion pipeline.

| Resource | Link |
|---|---|
| Software Requirements Specification (SRS) | [View SRS](./docs/Demo2/srs.md) |
| (SAS) | [View.SAS](./docs/Demo2/sas.md)|
| GitHub Project Board | [Open Board](#https://github.com/orgs/COS301-SE-2026/projects/46) |
| UI Wireframes & Designs | [Open Designs](./docs/design) |
| Brand Style Guide | [View Guide](./docs/design/brandStyleGuide.pdf) |
| Coding Standards | [View Standards](./docs/design/codingStandards.pdf) |
---

# Core Features

| Feature | What it does | Built with |
|---|---|---|
| **Shared Rulebook Library (The Vault)** | Upload, browse, read and collaboratively edit digitised rulebooks with version history | Spring Boot · FastAPI · MongoDB · Cloudflare R2 |
| **Marketplace** | Create, browse and manage peer-to-peer rental and sale listings; discover external retail sources | Spring Boot · MongoDB · Cloudflare R2 |
| **Community & Events** | Schedule public, friends-only and private events; RSVP and manage attendees | Spring Boot · MongoDB |
| **User Profiles & Social** | Manage profiles, game inventory, preferences, friends and groups | Spring Boot · Spring Security · JWT |

---

# Technology Stack

## Frontend & UI
![Vue](https://img.shields.io/badge/Vue.js-Framework-42b883?style=flat-square&logo=vue.js&logoColor=white)
![Nuxt](https://img.shields.io/badge/Nuxt-BFF-00DC82?style=flat-square&logo=nuxt.js&logoColor=white)
![ScopedCSS](https://img.shields.io/badge/Scoped_CSS-Styling-264de4?style=flat-square&logo=css3&logoColor=white)

## Backend & Core
![SpringBoot](https://img.shields.io/badge/Spring_Boot-Services-6DB33F?style=flat-square&logo=springboot&logoColor=white)
![NodeJS](https://img.shields.io/badge/Node.js-BFF-339933?style=flat-square&logo=node.js&logoColor=white)
![MongoDB](https://img.shields.io/badge/MongoDB_Atlas-Database-47A248?style=flat-square&logo=mongodb&logoColor=white)
![R2](https://img.shields.io/badge/Cloudflare_R2-Object_Storage-F38020?style=flat-square&logo=cloudflare&logoColor=white)

## AI & Ingestion
![Python](https://img.shields.io/badge/Python-AI_Gateway-3776AB?style=flat-square&logo=python&logoColor=white)
![FastAPI](https://img.shields.io/badge/FastAPI-Async_Pipeline-009688?style=flat-square&logo=fastapi&logoColor=white)

## Infrastructure & DevOps
![Docker](https://img.shields.io/badge/Docker-Containers-2496ED?style=flat-square&logo=docker&logoColor=white)
![GitHubActions](https://img.shields.io/badge/GitHub_Actions-CI/CD-2088FF?style=flat-square&logo=githubactions&logoColor=white)
![Render](https://img.shields.io/badge/Render_/_Railway-Hosting-46E3B7?style=flat-square&logo=render&logoColor=white)

---

# Architecture at a Glance

Boardwise follows a **Client–Server** architecture using **Service-Oriented Architecture (SOA)** as the overarching style, with **Component-Based** and **Domain-Driven** design as client-mandated requirements. Each backend service is a bounded context with a **Layered (Controller → Service → Repository)** internal structure.

The AI ingestion pipeline in The Vault processes uploads asynchronously through sequential stages (Sanitise → Extract) via FastAPI.

```text
                    ┌──────────────────────────┐
                    │   Vue.js SPA (Browser)    │
                    └────────────┬─────────────┘
                                 │ HTTPS / REST + WebSocket
                    ┌────────────▼─────────────┐
                    │   Nuxt / Node.js BFF      │  ← Auth Guard, route splitting
                    └──┬──────────┬─────────┬───┘
          REST         │          │         │   REST (direct-to-service)
        ┌──────────────▼┐  ┌──────▼──────┐  ┌▼───────────────┐
        │ User Service  │  │ Marketplace │  │  The Vault      │
        │ (Spring Boot) │  │ (Spring Boot)│ │ Spring Boot +   │
        │ Auth · Social │  │ Listings    │  │ FastAPI Gateway │
        └───────┬───────┘  └──────┬──────┘  └───────┬─────────┘
                │                 │                 │
        ┌───────▼─────────────────▼─────────────────▼─────────┐
        │      MongoDB Atlas  ·  Cloudflare R2 (PDFs/images)   │
        └──────────────────────────────────────────────────────┘
```

- **Event Sourcing** records every rulebook edit as an immutable event for full auditability.
- **WebSocket push** powers real-time collaborative editing with MRSW (Multi-Reader Single-Writer) locking.

See the full [SRS](./docs/Demo2/srs.md) for component diagrams, API service contracts and the traceability matrix.

---

# Team

Boardwise is developed by a multidisciplinary team of University of Pretoria Computer Science students.

| Team Member | Role | Focus Area | LinkedIn |
|---|---|---|---|
| **Hayley Booysen** *(Team Lead)* | Project Manager · UI Engineer | Project coordination, frontend architecture, UI/UX direction | [LinkedIn](https://www.linkedin.com/in/hayley-booysen-9372a9252/) |
| **Karabo Nkomo** | Services Engineer · Systems Architect | Backend services, system architecture, deployment | [LinkedIn](https://www.linkedin.com/in/karabo-nkomo-37b5b5319/) |
| **Njabulo Mathonsi** | DevOps Engineer · Services Engineer | CI/CD, backend services, data flow & authentication | [LinkedIn](https://www.linkedin.com/in/njabulo-mathonsi-5126983aa/) |
| **Palesa Nkosi** | UI/UX Designer · UI Engineer | Responsive UI, accessibility, interface & experience design | [LinkedIn](https://www.linkedin.com/in/bridget-nkosi-03734834b) |
| **Bandile Mnyandu** | Services Engineer · Integration Engineer | Backend validation, integration, testing strategy | [LinkedIn](https://www.linkedin.com/in/bandile-mnyandu-900b96303/) |

---

# Repository Structure

Boardwise follows a modular monorepo focused on scalability, maintainability, and separation of concerns.

```bash
boardwise/
├── .github/                # GitHub workflows and CI/CD
├── ai/                     # FastAPI AI Gateway (PDF ingestion pipeline)
│   ├── app/
│   │   ├── models/
│   │   ├── pipeline/       # Sanitise → Extract
│   │   ├── routers/
│   │   └── services/
│   ├── tests/
│   └── main.py
│
├── backend/                # Spring Boot services
│   └── src/main/java/com/boardwise/backend/
│       ├── user_service/   # Auth, profiles, social, events
│       ├── marketplace/    # Listings CRUD
│       ├── vault/          # Collaborative library + MRSW locking
│       ├── databaseimages/
│       └── shared/
│
├── frontend/               # Vue / Nuxt application
│   ├── components/         # Reusable UI + feature components
│   │   ├── features/       # auth, chat, community, library, marketplace, profile
│   │   ├── layout/
│   │   └── ui/             # Base components (BaseButton, BaseSearch, …)
│   ├── composables/
│   ├── pages/              # auth, community, events, library, marketplace, profile
│   ├── services/           # API clients (authService, libraryService, …)
│   ├── public/
│   └── tests/
│
├── docs/                   # SRS, brand guide, wireframes, design tokens
└── README.md
```

---

# Branching Strategy

Boardwise follows a structured multi-development-branch workflow.

```text
main
└── dev
    ├── frontend-dev
    │   └── feature/*
    ├── backend-dev
    │   └── feature/*
    └── integration
        └── feature/*
```

- **`main`** — stable, production-ready; only tested and reviewed code is merged here.
- **`dev`** — central development branch; integrates all streams before promotion to `main`.
- **`frontend-dev`** — primary frontend development branch.
- **`backend-dev`** — primary backend development branch.
- **`integration`** — system-wide integration and combined testing.
- **`feature/*`** — individual features branch from their respective parent branch, merged via pull request.

Every pull request requires **peer review**, **passing tests**, and **clean integration** into the target branch.

---

# Getting Started

```bash
# Clone the repository
git clone https://github.com/your-org/boardwise.git
cd boardwise

# Frontend
cd frontend && npm install && npm run dev

# Backend (Spring Boot services)
cd backend && ./mvnw spring-boot:run

# AI Gateway
cd ai && pip install -r requirements.txt && uvicorn main:app --reload
```

---

# Documentation

| Document | Description |
|---|---|
| [SRS](./docs/Demo2/srs.md)  | Functional & non-functional requirements, use cases, domain model, API contracts, architecture |
| [Brand Style Guide](./docs/design/brandStyleGuide%20(version%202).pdf) | Colour palette, typography, components, accessibility |
| [Wireframes](./docs/design) | UI/UX designs and navigation flows |
| [Design Tokens](./frontend/assets/theme.css) | Global styling variables for frontend consistency |

---

# Project Goals

- Digitise the South African tabletop gaming experience
- Strengthen local gaming communities through events and groups
- Improve rulebook accessibility via a collaborative shared library
- Enable peer-to-peer rentals and sales without retailer lock-in
- Build a maintainable, scalable, free-tier-hosted, open-source platform

---

<div align="center">

Built for the **COS 301 Capstone Project 2026**
University of Pretoria · in partnership with EPI-USE Labs

**Works On My Machine™**

</div>
