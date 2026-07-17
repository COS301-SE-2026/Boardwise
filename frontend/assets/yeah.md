# Boardwise — Brand Style Guide (Supplementary Sections)

  

*These sections extend the existing Boardwise Brand Style Guide (Colour Styles and Typography Styles already defined). They cover Design Principles, UI Component Styling, and Accessibility, and align directly with the design tokens in `frontend/assets/theme.css`.*

  

---

  

## 1. Design Principles

  

The Boardwise interface is guided by five principles. Every screen and component decision should be traceable to at least one of them.

  

### 1.1 Consistency

The same action looks and behaves the same way everywhere. Colours, typography, spacing, and component behaviour are defined once in the global design tokens (`theme.css`) and reused — never re-invented per page. A primary button on the Marketplace is identical to a primary button in The Vault.

  

### 1.2 Simplicity

Each screen has one clear primary action. Secondary options are present but visually subordinate. The interface favours progressive disclosure — complex flows such as creating a listing or uploading a rulebook are broken into focused steps rather than a single dense form.

  

### 1.3 Responsiveness

The platform is mobile-first, in line with the client's mandate. Layouts reflow gracefully from a single-column mobile view (with a bottom navigation bar) to a multi-column desktop view (with a side navigation bar). Components scale fluidly across the mobile, tablet, and desktop breakpoints.

  

### 1.4 Accessibility

The interface is usable by everyone, including users relying on screen readers or keyboard navigation. Accessibility is a baseline requirement, not an enhancement — see Section 3.

  

### 1.5 Clarity & Feedback

The system always communicates its state. Loading skeletons appear during data fetches, success and error states are explicit, and complex interactions carry contextual tooltips (e.g. uploading to The Vault, setting up a P2P rental). Users are never left guessing whether an action succeeded.

  

---

  

## 2. UI Component Styling

  

All components draw their values from the global design tokens. The standard styles below ensure uniform implementation across every feature module.

  

### 2.1 Buttons

  

| Variant | Usage | Background | Text | Border |

|---|---|---|---|---|

| **Primary** | The main action on a screen (Add, Save, Confirm) | Maroon `#6D0037` | White | None |

| **Secondary** | Supporting actions (Filters, Cancel) | Transparent | Maroon | `#C9C3D0` |

| **Accent** | High-urgency calls to action | Coral `#E4572E` | White | None |

  

- Padding: `12px 20px` · Radius: `10px` · Font: Hammersmith One.

- Hover darkens the fill (maroon → `#4F0028`); active state nudges down 1px.

- Focus shows a `3px` violet focus ring (`#A78BFA`) — never remove focus outlines.

- Disabled buttons drop to 50% opacity with a `not-allowed` cursor.

  

### 2.2 Cards

Used for game listings, rulebook entries, and event cards.

- Background white, `1px` border `#E5E1E8`, radius `16px`, subtle shadow.

- Hover lifts the shadow from `sm` to `md` to signal interactivity.

- Internal padding `24px`; consistent cover-image → title → metadata vertical rhythm.

  

### 2.3 Forms & Inputs

- Full-width inputs, padding `12px 16px`, radius `10px`, `1px` border `#C9C3D0`.

- Focus state: maroon border plus a soft maroon glow (`0 0 0 3px rgba(109,0,55,0.15)`).

- Every input has an associated, visible `<label>`. Placeholders are hints, never labels.

- Inline validation messages appear directly beneath the field in error red `#C62828`.

  

### 2.4 Navigation

- **Desktop:** persistent left side-navigation bar (`248px`) — Community, Marketplace, Library, Events, Profile.

- **Mobile:** bottom navigation bar with the same destinations as icons.

- Header height `64px`, containing the logo, global search, "Ask AI", and notifications.

- The active destination is indicated with the maroon brand colour and a clear selected state.

  

### 2.5 Modals & Pop-ups

Used for Add Game, Add Listing, and Upload Rulebook flows.

- Centred overlay on a dimmed scrim; surface white with radius `16px` and `lg` shadow.

- A clear title, the form body, and a right-aligned Cancel / Confirm button pair.

- Dismissible via the Cancel button, the close icon, the `Esc` key, and a scrim click.

  

### 2.6 Badges & Tags

- Used for genres, categories, listing type (For Rent / For Sale), and condition.

- Pill-shaped, soft-gold background `#E6C58A` with navy text by default; semantic colours for status.

  

### 2.7 Loading & Empty States

- All data-fetching views show skeleton placeholders (not spinners) for graceful degradation on slow connections.

- Empty states carry a short message and a clear next action (e.g. "No rulebooks found — upload one").

  

---

  

## 3. Accessibility

  

Boardwise targets **WCAG 2.1 Level AA** conformance (NFR2.2). The following are mandatory.

  

### 3.1 Colour & Contrast

- Body text against its background must meet a minimum contrast ratio of **4.5:1**; large text (≥24px or ≥18px bold) at least **3:1**.

- Maroon `#6D0037` on white and white text on maroon both exceed 4.5:1 and are the safe default text/background pairing.

- Colour is never the *only* means of conveying information — status uses an icon or label in addition to colour (e.g. listing condition, validation errors).

  

### 3.2 Keyboard Navigation

- Every interactive element is reachable and operable by keyboard alone, in a logical tab order.

- A **visible focus indicator** (the violet focus ring) is present on all focusable elements — focus outlines are never disabled.

- Modals trap focus while open and return focus to the triggering element on close. `Esc` closes them.

  

### 3.3 Screen Reader Support

- Semantic HTML first (`<button>`, `<nav>`, `<main>`, `<label>`); ARIA only to fill gaps.

- All meaningful images and game covers carry descriptive `alt` text; decorative images use empty `alt`.

- Icon-only controls (notifications, "Ask AI", close) have accessible names via `aria-label`.

- Live regions announce asynchronous updates (e.g. "Rulebook uploaded", real-time edit banners in The Vault).

  

### 3.4 Forms

- Each field is programmatically associated with its label.

- Errors are announced and described in text, not signalled by colour alone.

- Required fields are marked both visually and via `aria-required`.

  

### 3.5 Responsive & Touch

- Touch targets are at least `44 × 44px` on mobile.

- The layout remains usable and reflows without horizontal scrolling down to a `320px` viewport.

- Content and functionality are preserved at up to 200% zoom.

  

---

  

*All values in this guide are implemented in and should be consumed from `frontend/assets/theme.css`. If a value here and in the tokens file ever disagree, the tokens file is authoritative and this guide should be updated to match.*

# Boardwise — Brand Style Guide (Supplementary Sections)

_These sections extend the existing Boardwise Brand Style Guide (Colour Styles and Typography Styles already defined). They cover Design Principles, UI Component Styling, and Accessibility, and align directly with the design tokens in `frontend/assets/theme.css`._

---

## 1. Design Principles

The Boardwise interface is guided by five principles. Every screen and component decision should be traceable to at least one of them.

### 1.1 Consistency

The same action looks and behaves the same way everywhere. Colours, typography, spacing, and component behaviour are defined once in the global design tokens (`theme.css`) and reused — never re-invented per page. A primary button on the Marketplace is identical to a primary button in The Vault.

### 1.2 Simplicity

Each screen has one clear primary action. Secondary options are present but visually subordinate. The interface favours progressive disclosure — complex flows such as creating a listing or uploading a rulebook are broken into focused steps rather than a single dense form.

### 1.3 Responsiveness

The platform is mobile-first, in line with the client's mandate. Layouts reflow gracefully from a single-column mobile view (with a bottom navigation bar) to a multi-column desktop view (with a side navigation bar). Components scale fluidly across the mobile, tablet, and desktop breakpoints.

### 1.4 Accessibility

The interface is usable by everyone, including users relying on screen readers or keyboard navigation. Accessibility is a baseline requirement, not an enhancement — see Section 3.

### 1.5 Clarity & Feedback

The system always communicates its state. Loading skeletons appear during data fetches, success and error states are explicit, and complex interactions carry contextual tooltips (e.g. uploading to The Vault, setting up a P2P rental). Users are never left guessing whether an action succeeded.

---

## 2. UI Component Styling

All components draw their values from the global design tokens. The standard styles below ensure uniform implementation across every feature module.

### 2.1 Buttons

|Variant|Usage|Background|Text|Border|
|---|---|---|---|---|
|**Primary**|The main action on a screen (Add, Save, Confirm)|Maroon `#6D0037`|White|None|
|**Secondary**|Supporting actions (Filters, Cancel)|Transparent|Maroon|`#C9C3D0`|
|**Accent**|High-urgency calls to action|Coral `#E4572E`|White|None|

- Padding: `12px 20px` · Radius: `10px` · Font: Hammersmith One.
- Hover darkens the fill (maroon → `#4F0028`); active state nudges down 1px.
- Focus shows a `3px` violet focus ring (`#A78BFA`) — never remove focus outlines.
- Disabled buttons drop to 50% opacity with a `not-allowed` cursor.

### 2.2 Cards

Used for game listings, rulebook entries, and event cards.

- Background white, `1px` border `#E5E1E8`, radius `16px`, subtle shadow.
- Hover lifts the shadow from `sm` to `md` to signal interactivity.
- Internal padding `24px`; consistent cover-image → title → metadata vertical rhythm.

### 2.3 Forms & Inputs

- Full-width inputs, padding `12px 16px`, radius `10px`, `1px` border `#C9C3D0`.
- Focus state: maroon border plus a soft maroon glow (`0 0 0 3px rgba(109,0,55,0.15)`).
- Every input has an associated, visible `<label>`. Placeholders are hints, never labels.
- Inline validation messages appear directly beneath the field in error red `#C62828`.

### 2.4 Navigation

- **Desktop:** persistent left side-navigation bar (`248px`) — Community, Marketplace, Library, Events, Profile.
- **Mobile:** bottom navigation bar with the same destinations as icons.
- Header height `64px`, containing the logo, global search, "Ask AI", and notifications.
- The active destination is indicated with the maroon brand colour and a clear selected state.

### 2.5 Modals & Pop-ups

Used for Add Game, Add Listing, and Upload Rulebook flows.

- Centred overlay on a dimmed scrim; surface white with radius `16px` and `lg` shadow.
- A clear title, the form body, and a right-aligned Cancel / Confirm button pair.
- Dismissible via the Cancel button, the close icon, the `Esc` key, and a scrim click.

### 2.6 Badges & Tags

- Used for genres, categories, listing type (For Rent / For Sale), and condition.
- Pill-shaped, soft-gold background `#E6C58A` with navy text by default; semantic colours for status.

### 2.7 Loading & Empty States

- All data-fetching views show skeleton placeholders (not spinners) for graceful degradation on slow connections. Use the shared `.skeleton` class, which is driven by the `--skeleton-base` / `--skeleton-highlight` tokens and respects reduced-motion.
- Empty states carry a short message and a clear next action (e.g. "No rulebooks found — upload one").

> **Token reference:** Navigation styling is centralised in the `--nav-*` tokens (`--nav-bg`, `--nav-active`, `--nav-hover`, `--nav-text`); modals use `--modal-overlay` and `--modal-max-width`; page width is governed by the `.container` utility (`--container-max`). Consume these rather than re-declaring values per component.

---

## 3. Accessibility

Boardwise targets **WCAG 2.1 Level AA** conformance (NFR2.2). The following are mandatory.

### 3.1 Colour & Contrast

- Body text against its background must meet a minimum contrast ratio of **4.5:1**; large text (≥24px or ≥18px bold) at least **3:1**.
- Maroon `#6D0037` on white and white text on maroon both exceed 4.5:1 and are the safe default text/background pairing.
- Colour is never the _only_ means of conveying information — status uses an icon or label in addition to colour (e.g. listing condition, validation errors).

### 3.2 Keyboard Navigation

- Every interactive element is reachable and operable by keyboard alone, in a logical tab order.
- A **visible focus indicator** (the violet focus ring) is present on all focusable elements — focus outlines are never disabled.
- Modals trap focus while open and return focus to the triggering element on close. `Esc` closes them.

### 3.3 Screen Reader Support

- Semantic HTML first (`<button>`, `<nav>`, `<main>`, `<label>`); ARIA only to fill gaps.
- All meaningful images and game covers carry descriptive `alt` text; decorative images use empty `alt`.
- Icon-only controls (notifications, "Ask AI", close) have accessible names via `aria-label`.
- Live regions announce asynchronous updates (e.g. "Rulebook uploaded", real-time edit banners in The Vault).

### 3.4 Forms

- Each field is programmatically associated with its label.
- Errors are announced and described in text, not signalled by colour alone.
- Required fields are marked both visually and via `aria-required`.

### 3.5 Responsive & Touch

- Touch targets are at least `44 × 44px` on mobile.
- The layout remains usable and reflows without horizontal scrolling down to a `320px` viewport.
- Content and functionality are preserved at up to 200% zoom.

---

## 4. Layout & Structure

### 4.1 Grid & Layout System

Boardwise uses a **12-column responsive grid** as the basis for all page layouts, centred within a `--container-max` of `1200px`.

- **Gutters:** `24px` (`--space-6`) between columns on desktop, `16px` (`--space-4`) on mobile.
    
- **Outer margins:** `24px` minimum on mobile, growing to centred whitespace on wide screens.
    
- **Breakpoints** (mobile-first — styles cascade upward):
    
    |Range|Width|Columns|Navigation|
    |---|---|---|---|
    |Mobile|`< 480px`|4|Bottom bar|
    |Tablet|`480–1023px`|8|Bottom bar / collapsible|
    |Desktop|`≥ 1024px`|12|Left side bar (`248px`)|
    
- **Page shell:** on desktop, a fixed `64px` header plus a `248px` side navigation; the main content occupies the remaining space. On mobile, the side nav collapses into the bottom navigation bar and content goes full-width.
    
- **Card grids** (Marketplace, Library) use CSS Grid with `repeat(auto-fill, minmax(220px, 1fr))` so cards reflow naturally without per-breakpoint rules.
    
- Prefer **Flexbox** for one-dimensional component layouts (toolbars, button rows) and **CSS Grid** for two-dimensional page and card layouts.
    

### 4.2 Z-index Layering Rules

A fixed scale prevents stacking conflicts. Never invent arbitrary `z-index` values — use these tiers (add them to `theme.css` as `--z-*` tokens):

|Layer|Token|Value|Used by|
|---|---|---|---|
|Base|`--z-base`|`0`|Default page content|
|Raised|`--z-raised`|`10`|Hover-lifted cards, sticky sub-headers|
|Navigation|`--z-nav`|`100`|Side bar, bottom bar, app header|
|Dropdown|`--z-dropdown`|`200`|Menus, select popovers, tooltips|
|Overlay|`--z-overlay`|`1000`|Modal scrim|
|Modal|`--z-modal`|`1010`|Modal/dialog content|
|Toast|`--z-toast`|`1100`|Toasts and notifications (always on top)|

Rule of thumb: a higher-tier element always renders above everything in lower tiers. Toasts sit above modals so feedback is never hidden.

---

## 5. Motion & Animation Guidelines

Motion should be **purposeful, quick, and subtle** — it communicates state and continuity, never decoration for its own sake.

- **Durations:** `120ms` (`--transition-fast`) for micro-interactions (hover, active), `200ms` (`--transition-base`) for state changes (modal open, accordion). Nothing user-blocking should exceed `300ms`.
    
- **Easing:** use `ease` / `ease-out` for entrances and `ease-in` for exits. Avoid linear motion except for continuous spinners.
    
- **Standard patterns:**
    
    - _Buttons_ — background colour transition on hover, `translateY(1px)` on active.
    - _Cards_ — shadow lifts from `sm` to `md` on hover.
    - _Modals_ — fade the scrim in and scale content from `0.98 → 1` on open.
    - _Page/list loads_ — staggered fade-in of skeletons, then content.
    - _Toasts_ — slide in from the edge, auto-dismiss with a fade.
- **Reduced motion:** always honour the user's system preference. Wrap non-essential animation so it is disabled under `prefers-reduced-motion`:
    
    ```css
    @media (prefers-reduced-motion: reduce) {
      *, *::before, *::after {
        animation-duration: 0.01ms !important;
        transition-duration: 0.01ms !important;
      }
    }
    ```
    

---

## 6. Additional Components

### 6.1 Toast / Notification Styling

Toasts deliver transient, non-blocking feedback (e.g. "Rulebook uploaded", "Listing created").

- **Placement:** top-right on desktop, top-centre full-width on mobile; sits at `--z-toast`.
    
- **Anatomy:** a leading status icon, a short message, and an optional dismiss control. Keep copy to one line where possible.
    
- **Variants** (left border + icon colour carry the semantics, never colour alone):
    
    |Type|Accent|Icon|
    |---|---|---|
    |Success|`--color-success` `#2E7D5B`|check|
    |Error|`--color-error` `#C62828`|alert|
    |Warning|`--color-warning` `#B7791F`|warning|
    |Info|`--color-info` (violet)|info|
    
- **Behaviour:** auto-dismiss after ~4s (errors persist until dismissed); stack vertically with `8px` gaps; announce via an `aria-live="polite"` region (`assertive` for errors).
    
- Surface white, radius `10px`, `md` shadow, `12px 16px` padding.
    

### 6.2 Table Styling

Tables are used sparingly (e.g. admin moderation, listing management) — prefer cards for browsing.

- **Header row:** `--color-surface-alt` background, navy text, `font-medium`, left-aligned.
- **Body rows:** white, separated by a `1px` `--color-border` bottom rule; subtle `--color-surface-alt` on row hover.
- **Cell padding:** `12px 16px`; numeric columns right-aligned.
- **Zebra striping** is optional and, if used, applies a very light `--color-surface-alt` to alternate rows — never both striping and hover-emphasis at once.
- **Responsive:** below the tablet breakpoint, tables either scroll horizontally within a bordered container or collapse to stacked label–value cards.
- Always include a `<caption>` (visually hidden if needed) and proper `<th scope>` for accessibility.

---

## 7. Engineering Standards

### 7.1 Dark Mode Strategy

Dark mode is **not supported in the current scope.** This is a deliberate decision, recorded here so it is intentional rather than accidental:

- The platform ships **light theme only** for Demo 1 and the current milestone, keeping design and QA effort focused.
- However, the token architecture is **dark-mode-ready**: because all components consume semantic tokens (`--color-bg`, `--color-surface`, `--color-text`) rather than raw brand colours, adding dark mode later is a matter of redefining those tokens under a `[data-theme="dark"]` selector — no component rewrites required.
- User-selectable **app theming** (per the client's optional requirement) is treated as a separate future feature and is also enabled by this token approach.
- Until then, components must **not** hard-code light-only values (e.g. `#FFFFFF`); they must use the semantic surface/text tokens so a future switch is low-cost.

### 7.2 Component Naming Conventions

Consistent names make components discoverable and predictable across the team.

- **Vue component files:** `PascalCase`, grouped by domain — `RulebookCard.vue`, `MarketplaceHeader.vue`, `BaseButton.vue`.
- **Base/primitive components** are prefixed `Base` (`BaseButton`, `BaseSearch`, `BaseModal`); **feature components** live under `components/features/<domain>/`.
- **CSS classes** follow a BEM-style convention: block, `block__element`, `block--modifier` — e.g. `.card`, `.card__title`, `.btn--primary`. This mirrors the modifier classes already in `theme.css`.
- **Design tokens** are kebab-case with a domain prefix: `--color-*`, `--space-*`, `--fs-*`, `--z-*`, `--bw-*` for raw brand values.
- **Composables:** camelCase with a `use` prefix — `useAuth`, `useLibrary`.
- **Events emitted** from components are kebab-case verbs — `@create-listing`, `@upload-rulebook`.

### 7.3 Design Token Usage Examples

Always consume tokens; never hard-code a brand value in a component.

```css
/* ✗ Do NOT do this — hard-coded, inconsistent, un-themeable */
.listing-button {
  background: #6D0037;
  padding: 11px 19px;
  border-radius: 9px;
  font-family: 'Hammersmith One';
}

/* ✓ Do this — uses tokens / primitives, stays consistent and themeable */
.listing-button {
  background: var(--color-primary);
  padding: var(--space-3) var(--space-5);
  border-radius: var(--radius-md);
  font-family: var(--font-button);
}
```

```vue
<!-- ✓ Even better: reuse the shared primitive class -->
<template>
  <button class="btn btn--primary" @click="$emit('create-listing')">
    + Create Listing
  </button>
</template>
```

```css
/* Spacing & type from the scale, not magic numbers */
.rulebook-card {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);            /* not "12px" */
  padding: var(--space-6);        /* not "24px" */
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
}
.rulebook-card__title { font-size: var(--fs-h4); color: var(--color-secondary); }
.rulebook-card__meta  { font-size: var(--fs-small); color: var(--color-text-muted); }
```

**Quick reference — what to reach for:**

|Need|Use|Not|
|---|---|---|
|Brand fill|`var(--color-primary)`|`#6D0037`|
|Spacing|`var(--space-4)`|`16px`|
|Heading size|`var(--fs-h2)`|`24px`|
|Corner radius|`var(--radius-md)`|`10px`|
|Stacking|`var(--z-modal)`|`z-index: 9999`|
|A button|`.btn .btn--primary`|bespoke per-page styles|

---

_All values in this guide are implemented in and should be consumed from `frontend/assets/theme.css`. If a value here and in the tokens file ever disagree, the tokens file is authoritative and this guide should be updated to match._