<template>
  <section class="py-8 section-border" id="changelog">
    <SectionTitle 
      title="10 - Changelog from Demo 1" 
      subtitle="What changed since Demo 1 guide, and why. Full rebrand of colour and type; 
      component values evolved from Figma spec to implemented CSS tokens."
    />

    <v-row class="mt-4">
        <v-col cols="12">
            <v-card flat border class="pa-4 mb-4">
                <div class="change-title mb-2">
                    Colour palette - full replacement
                </div>

                <table class="change-table">
                    <thead>
                        <tr><th>Role</th><th>Demo 1</th><th>Demo 2</th><th>Rationale</th></tr>
                    </thead>

                    <tbody>
                        <tr>
                            <td>Primary</td>
                            <td>Gold <code>#C9A86E</code></td>
                            <td>Wildfire <code>#C7286E</code></td>
                            <td>Gold read as generic/corporate; Wildfire ties to the "event card" brand story and gives a stronger,
                                more distinctive primary action colour. 
                            </td>
                        </tr>

                        <tr>
                            <td>Button bg (undocumented in Demo 1's own palette table)</td>
                            <td>Maroon <code>#6D0037</code> / Merlot <code>#8E1B4E</code> hover</td>
                            <td>Demo 1 referenced Maroon in the component spec but never catalogued it as a token - 
                                consolidated into the new Wildfire/Merlot pair so every button colour is traceable 
                                to a named token
                            </td>
                        </tr>

                        <tr>
                            <td>Accent (CTA)</td>
                            <td>Coral <code>#E4573E</code> / Orange <code>#F97316</code></td>
                            <td>Copper <code>#EF5B27</code> - <strong>retired</strong></td>
                        </tr>

                        <tr>
                            <td>Focus / Secondary accent</td>
                            <td>Violet <code>#7C3AED</code> / Light violet <code>#A78BFA</code></td>
                            <td>Obsidain <code>#4E1E5C</code></td>
                            <td>Purple carried forward as a brand colour (Obsidian), but its Demo 1 job - the focus ring -
                                didn't fully migrate. Flagged in Do's & Don'ts as an open fix: Demo 3 will be changed.
                            </td>
                            <td>Cool grey neutrals swapped for warm, paper/felt-toned neutrals to match the "tabletop" brand metaphor introduced in Demo 2.</td>
                        </tr>
                    </tbody>
                </table>
            </v-card>

            <v-card flat border class="pa-4 mb-4">
                <div class="change-title mb-2">Typography - full replacement</div>

                <table class="change-table">
                    <thead>
                        <tr><th>Role</th><th>Demo 1</th><th>Demo 2</th><th>Rationale</th></tr>
                    </thead>

                    <tbody>
                        <tr>
                            <td>Headings</td>
                            <td>Myanmar Khyay</td>
                            <td>DynaPuff</td>
                            <td>Myanmar Khyay is a script-specific font with no thematic tie to the brand;
                                DynaPuff's rounded, playful forms better match a board-game platforms's voice.
                            </td>
                        </tr>

                        <tr>
                            <td>Body</td>
                            <td>Inter</td>
                            <td>Plus Jakarta Sans</td>
                            <td>Similar geomretric feel to Inter but slightly warmer letterforms, better pairing with DynaPuff headings.</td>
                        </tr>

                        <tr>
                            <td>Buttons</td>
                            <td>Hammersmith One</td>
                            <td>Plus Jakarta Sans</td>
                            <td>Reduced to two font families total for simpler maintenance and fewer web font requests.</td>
                        </tr>
                    </tbody>
                </table>
            </v-card>

            <v-card flat border class="pa-4 mb-4">
                <div class="change-title mb-2">Components - evolved, not replaced</div>
                <ul class="change-list">
                    <li>
                        <strong>Buttons:</strong> radius changed from 10px to a full pill (999px); disabled state (50% opacity, not-allowed cursor) and active-state 1px nudge 
                        carried over unchanged from Demo 1.
                    </li>

                    <li>
                        <strong>Cards:</strong> radius increased from 16px to 24px; padding (24px) and the hover shadow-sm to shadow-md pattern carried over unchanged from
                        Demo 1. 
                    </li>

                    <li>
                        <strong>Badges/tags:</strong> soft-gold pill concept carried over in spirit, now using 
                        Rosewood in place of the retired gold/coral tones.
                    </li>
                </ul>
            </v-card>

            <v-card flat border class="pa-4">
                <div class="change-title mb-2">Carried forward unchanged</div>
                <p class="rule-copy">
                    The five Design Principles from Demo 1 - Consistency, Simplicity, Responsiveness, Accessibility, and Clarity
                    Feedback - still hold and re not restated here in full; see Section 06 (Accessibility) and Section 07 (Voice & Tone) 
                    for how they're now expressed in implemented components rather than standalone guidance.
                </p>
            </v-card>
        </v-col>
    </v-row>
  </section>
</template>

<script setup>
import SectionTitle from '~/components/ui/SectionTitle.vue'
</script>

<style scoped>
.section-border {
  border-top: 1px solid var(--color-border);
}

.change-title {
    font-family: var(--font-display);
    font-size: var(--fs-h4);
    font-weight: var(--fw-bold);
    color: var(--color-secondary);
}

.change-table {
    width: 100%;
    border-collapse: collapse;
    font-size: var(--fs-small);
}

.change-table th {
    text-align: left;
    padding: 8px;
    color: var(--color-secondary);
    border-bottom: 2px solid var(--color-border);
}

.change-table td {
    padding: 8px;
    vertical-align: top;
    color: var(--color-text-muted);
    border-bottom: 1px solid var(--color-border);
}

.change-table code {
    font-family: ui-monospace, 'SF Mono', Menlo, monospace;
    background: var(--feltboard);
    padding: 1px 6px;
    border-radius: 3px;
    font-size: 0.75rem;
}

.change-list {
    padding-left: 20px;
    font-size: var(--fs-small);
    color: var(--color-text-muted);
    display: flex;
    flex-direction: column;
    gap: 8px;
    margin: 0px;
}
 
.rule-copy {
  font-size: var(--fs-small);
  color: var(--color-text-muted);
  margin: 0;
}
</style>