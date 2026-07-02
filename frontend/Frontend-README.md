# Pulling the New Vuetify Setup (Frontend Team Guide)

The `frontend-dev` branch now includes the initial Vuetify setup and Boardwise design system integration.

Follow these steps after pulling the latest changes.

---

# 1. Checkout `frontend-dev`

```bash id="3cm0zr"
git checkout frontend-dev
```

---

# 2. Pull Latest Changes

```bash id="m0e5q6"
git pull origin frontend-dev
```

---

# 3. Install New Dependencies

Vuetify and supporting packages were added.

Run:

```bash id="22g0od"
npm install
```

This installs:

* Vuetify
* vuetify-nuxt-module
* Sass
* Material Design Icons

---

# 4. Verify New Files Exist

The following files should now exist:

```txt id="39vt2z"
assets/theme.css
assets/settings.scss
```

These files contain:

* global design tokens
* typography
* spacing
* colour system
* Vuetify component configuration

---

# 5. Verify `nuxt.config.js`

Ensure the project includes:

```js id="5k8hxh"
modules: ['vuetify-nuxt-module']
```

and:

```js id="zv0s2u"
css: ['~/assets/theme.css']
```

---

# 6. Run the Development Server

```bash id="9a6xum"
npm run dev
```

---

# 7. Confirm Vuetify Is Working

Test with a simple component:

```vue id="2d9i5v"
<v-btn color="primary">
  Test Button
</v-btn>
```

If styled correctly, Vuetify is installed successfully.

---

# 8. New Frontend Development Guidelines

## Use Vuetify Components Instead of Raw HTML

Preferred replacements:

| Old          | New              |
| ------------ | ---------------- |
| `<button>`   | `<v-btn>`        |
| `<input>`    | `<v-text-field>` |
| `<textarea>` | `<v-textarea>`   |
| `<select>`   | `<v-select>`     |
| Custom modal | `<v-dialog>`     |
| `.card` divs | `<v-card>`       |

---

# 9. Use Vuetify Utility Classes

Instead of writing custom flexbox CSS:

```vue id="jlwmqv"
class="d-flex align-center justify-space-between ga-4 pa-4"
```

Useful utilities:

| Utility                 | Purpose            |
| ----------------------- | ------------------ |
| `d-flex`                | display flex       |
| `align-center`          | align items center |
| `justify-space-between` | spacing            |
| `ga-4`                  | gap                |
| `pa-4`                  | padding            |
| `ma-4`                  | margin             |

---

# 10. Design System Notes

The project now uses:

* `theme.css` → design tokens
* `settings.scss` → Vuetify styling defaults
* Vuetify → primary component library

Avoid:

* creating new custom button systems
* excessive layout CSS
* duplicate component styling

Prefer:

* Vuetify components
* Vuetify utility classes
* reusable wrapper components

---

# 11. Migration Strategy Going Forward

Frontend migration will happen incrementally.

Recommended order:

1. Layout/navigation
2. Buttons
3. Inputs/forms
4. Cards/dialogs
5. Feature pages

This prevents large merge conflicts and keeps the app stable during migration.
