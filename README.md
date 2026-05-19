<div align="center">

# BOARDWISE

### PLAY • SHARE • CONNECT

Boardwise is a comprehensive digital ecosystem for board game enthusiasts that optimises collection management, community engagement, and rulebook accessibility into one platform.

---

![Vue](https://img.shields.io/badge/Vue-Frontend-42b883?style=for-the-badge&logo=vue.js&logoColor=white)
![Nuxt](https://img.shields.io/badge/Nuxt-Fullstack-00DC82?style=for-the-badge&logo=nuxt.js&logoColor=white)
![SpringBoot](https://img.shields.io/badge/SpringBoot-Backend-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![FastAPI](https://img.shields.io/badge/FastAPI-AI_API-009688?style=for-the-badge&logo=fastapi&logoColor=white)
![MongoDB](https://img.shields.io/badge/MongoDB-Database-47A248?style=for-the-badge&logo=mongodb&logoColor=white)

---

# SHARED LIBRARY • MARKETPLACE • AI RULE ASSISTANT

*A COS301 Capstone Project — University of Pretoria*

</div>

---

# Functional Requirements

| Resource | Link |
|---|---|
| Software Requirements Specification | [View SRS](#) |
| GitHub Project Board | [Open Board](#) |
| Issue Tracker | [View Issues](#) |
| UI Wireframes | [Open Designs](#) |

---

# Technology Stack

## Frontend & UI

![Vue](https://img.shields.io/badge/Vue.js-Framework-42b883?style=flat-square&logo=vue.js&logoColor=white)
![Nuxt](https://img.shields.io/badge/Nuxt-App_Framework-00DC82?style=flat-square&logo=nuxt.js&logoColor=white)
![ScopedCSS](https://img.shields.io/badge/ScopedCSS-Styling-blue?style=flat-square)

---

## Backend & Core

![NodeJS](https://img.shields.io/badge/Node.js-Backend-339933?style=flat-square&logo=node.js&logoColor=white)
![SpringBoot](https://img.shields.io/badge/SpringBoot-Services-6DB33F?style=flat-square&logo=springboot&logoColor=white)
![MongoDB](https://img.shields.io/badge/MongoDB-Database-47A248?style=flat-square&logo=mongodb&logoColor=white)

---

## AI & Machine Learning

![Python](https://img.shields.io/badge/Python-AI_Services-3776AB?style=flat-square&logo=python&logoColor=white)
![RAG](https://img.shields.io/badge/RAG-Rulebook_Search-purple?style=flat-square)
![LLM](https://img.shields.io/badge/LLM-AI_Assistant-orange?style=flat-square)

---

## Infrastructure & DevOps

![Docker](https://img.shields.io/badge/Docker-Containers-2496ED?style=flat-square&logo=docker&logoColor=white)
![GitHubActions](https://img.shields.io/badge/GitHub_Actions-CI/CD-2088FF?style=flat-square&logo=githubactions&logoColor=white)

---

# Core Features

## Shared Rulebook Library

- Upload and store board game rulebooks
- Browse community rulebooks
- Read rulebooks digitally
- Maintain updated editions collaboratively

---

## AI Rule Assistant

- Ask gameplay questions in natural language
- Receive instant AI-generated answers
- Reduce gameplay interruptions
- Improve accessibility for new players

---

## Marketplace

- Buy, sell, and rent board games
- Create listings
- Discover local offers
- Receive personalised recommendations

---

## Community Hub

- Create and join events
- Build local gaming groups
- Connect with nearby players
- Organise public and private sessions

---

# Design Requirements

Boardwise focuses on:

- Responsive design
- Accessibility-first interfaces
- Smooth cross-platform experiences
- Modern UI/UX principles
- Performance optimisation for mid-range devices

---

# Team

Boardwise is developed by a multidisciplinary team of University of Pretoria Computer Science students with strengths across frontend development, backend systems, DevOps, testing, integration, and user experience design.

| Team Member | Role | Focus Area | LinkedIn |
|---|---|---|---|
| **Hayley Booysen** | Project Manager • Frontend & QA Lead | UI/UX direction, frontend architecture, project coordination, testing & integration | [LinkedIn](https://www.linkedin.com/in/hayley-booysen-9372a9252/) |
| **Palesa Nkosi** | Frontend Developer & UI Designer | Responsive UI development, accessibility, interface design, user experience | [LinkedIn](https://www.linkedin.com/in/bridget-nkosi-03734834b) |
| **Njabulo Mathonsi** | Backend Developer | API development, scalable backend systems, authentication & data flow management | [LinkedIn](https://www.linkedin.com/in/njabulo-mathonsi-5126983aa/) |
| **Karabo Nkomo** | Backend & DevOps Support | Backend infrastructure, deployment systems, scalability & performance optimisation | [LinkedIn](https://www.linkedin.com/in/karabo-nkomo-37b5b5319/) |
| **Bandile Mnyandu** | QA & Backend Support | System reliability, backend validation, testing strategies & robustness | [LinkedIn](https://www.linkedin.com/in/bandile-mnyandu-900b96303/) |

---

# Repository Structure

Boardwise follows a modular architecture focused on scalability, maintainability, and separation of concerns.

```bash
boardwise/
│
├── .github/               # GitHub workflows and CI/CD
├── .vscode/               # Workspace settings
├── ai/                    # AI services and RAG systems
├── backend/               # Backend APIs and services
├── docs/                  # Documentation and SRS
├── frontend/              # Vue/Nuxt frontend application
│   ├── .nuxt/             # Nuxt generated files
│   ├── components/        # Reusable UI components
│   ├── composables/       # Shared Vue composables
│   ├── pages/             # Application pages/routes
│   ├── public/            # Static assets
│   ├── services/          # API and frontend services
│   ├── tests/             # Frontend tests
│   ├── app.vue            # Root Vue component
│   ├── nuxt.config.ts     # Nuxt configuration
│   └── package.json       # Frontend dependencies
│
├── tests/                 # Shared/integration tests
└── README.md
```

---

# Branching Strategy

Boardwise follows a structured multi-development branch workflow.

```text
main
├── frontend-dev
│   └── feature/*
│
├── backend-dev
│   └── feature/*
│
└── integration
    └── feature/*
```

### Workflow

- `main`
  - Stable production-ready branch
  - Only tested and reviewed code is merged here

- `frontend-dev`
  - Main frontend development branch

- `backend-dev`
  - Main backend development branch

- `integration`
  - Used for system-wide integration and combined testing

- `feature/*`
  - Individual features branch from their respective development branches
  - Merged through pull requests after testing and review

Every pull request requires:
- Peer review
- Passing tests
- Clean integration into the target branch
---

# Getting Started

## Clone Repository

```bash
git clone https://github.com/your-org/boardwise.git
```

---

## Install Dependencies

```bash
npm install
```

---

## Start Development Server

```bash
npm run dev
```

---

# Documentation

| Document | Description |
|---|---|
| SRS | Functional and architectural requirements |
| Wireframes | UI/UX designs and flows |
| API Contracts | Backend service contracts |
| Domain Model | UML and architecture diagrams |

---

# Project Goals

Boardwise aims to:

- Digitise the tabletop gaming experience
- Strengthen local gaming communities
- Improve rulebook accessibility
- Enhance gameplay through AI assistance
- Create a unified platform for board game enthusiasts

---

<div align="center">

Built for COS301 Capstone Project 2026  
University of Pretoria

</div>
