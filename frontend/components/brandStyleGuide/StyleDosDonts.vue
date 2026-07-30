<template>
  <section class="py-8 section-border" id="dos-donts">
    <SectionTitle 
      title="06 - Do's & Don'ts" 
      subtitle="Colour, type, focus states and motion rules for boardwise."
    />

    <div class="group-label mt-6">Colour</div>
    <v-row class="mt-2">
        <v-col cols="12" md="6">
            <v-card flat border class="rule-card dont pa-4 h-100">
                <div class="rule-card-title dont-title mb-3">
                    <v-icon>mdi-close-thick</v-icon>
                    Don't
                </div>

                <div class="rule-demo">
                    <span class="demo-chip copper-chip">Featured</span>
                    <span class="demo-chip copper-btn">For Rent</span>
                </div>

                <p class="rule-copy mt-3">
                    No Copper(<code>--copper</code>) usuage for important text or information. Use strictly for contrast.
                </p>
            </v-card>
        </v-col>

        <v-col cols="12" md="6">
            <v-card flat border class="rule-card do pa-4 h-100">
                <div class="rule-card-title dont-title mb-3">
                    <v-icon>mdi-check</v-icon>
                    Do
                </div>

                <div class="rule-demo">
                    <span class="demo-chip wildfire-chip">Featured</span>
                    <span class="demo-chip wildfire-btn">For Rent</span>
                </div>

                <p class="rule-copy mt-3">
                    Use Wildfire (<code>--wildfire</code>) for primary actions and emphasis, or can
                    fall back to Merlot for secondary accent.
                </p>
            </v-card>
        </v-col>

        <v-col cols="12" md="6">
            <v-card flat border class="rule-card dont pa-4 h-100">
                <div class="rule-card-title dont-title mb-3">
                    <v-icon>mdi-close-thick</v-icon>
                    Don't
                </div>

                <div class="rule-demo">
                    <div class="demo-block wildfire-obsidian">Obsidian text on Wildfire</div>
                </div>

                <p class="rule-copy mt-3">
                    Never pair Obsidian (<code>--obsidain</code>) with Wildfire (<code>--wildfire</code>)
                    - as text on background or adjacent blocks. Constrast is poor and the two reads as a clash.
                </p>
            </v-card>
        </v-col>

        <v-col cols="12" md="6">
            <v-card flat border class="rule-card do pa-4 h-100">
                <div class="rule-card-title dont-title mb-3">
                    <v-icon>mdi-check</v-icon>
                    Do
                </div>

                <div class="rule-demo">
                    <div class="demo-block wildfire-white">Alabaster text on Wildfire</div>
                </div>

                <p class="rule-copy mt-3">
                    Pair Wildfire with Alabaster (white) for text. Keep Obsidain for headings on neutral surfaces (Alabaster / Scrollpaper) instead.
                </p>
            </v-card>
        </v-col>
    </v-row>

    <div class="group-label mt-6">Typography</div>
    <v-row class="mt-2">
        <v-col cols="12" md="6">
            <v-card flat border class="rule-card do pa-4 h-100">
                <div class="rule-card-title dont-title mb-3">
                    <v-icon>mdi-check</v-icon>
                    Do
                </div>

                <p class="type-demo font-body">Your rulebook is ready to read.</p>

                <p class="rule-copy mt-3">
                    Body copy uses(<code>--font-body</code>) (Plus Jakarta Sans).
                </p>
            </v-card>
        </v-col>
    </v-row>

    <div class="group-label mt-6">Focus states</div>
    <v-row class="mt-2">
        <v-col cols="12" md="6">
            <v-card flat border class="rule-card dont pa-4 h-100">
                <div class="rule-card-title dont-title mb-3">
                    <v-icon>mdi-close-thick</v-icon>
                    Don't
                </div>

                <p class="rule-copy">
                    Don't mix focus styles: <code>.btn</code> instead swaps its border to 3px Rosewood outline,
                    but <code>.input</code> instead swaps its border to Wildfire. Two diffrent signals for the 
                    same state trains users to ignore both. Rosewood can disappear against Scrollpaper or light surface.
                </p>
            </v-card>
        </v-col>

        <v-col cols="12" md="6">
            <v-card flat border class="rule-card do pa-4 h-100">
                <div class="rule-card-title do-title mb-3">
                    <v-icon>mdi-check</v-icon>
                    Do
                </div>

                <p class="rule-copy">
                    Pick one focus treatment and use it everywhere - e.g. 3px outline in a colour that passes
                    3:1 every surface it can appear on (Wildfire or Obsidian test better than Rosewood). Apply
                    it the same way on buttons, inputs, links and cards.
                </p>
            </v-card>
        </v-col>
    </v-row>

    <div class="group-label mt-6">Legacy tokens</div>
    <v-row class="mt-2">
        <v-col cols="12">
            <v-card flat border class="rule-card dont pa-4 h-100">
                <div class="rule-card-title dont-title mb-3">
                    <v-icon>mdi-close-thick</v-icon>
                    Don't
                </div>
                <p class="rule-copy mt-3">
                    Don't reach for <code>--bw-maroon</code>, <code>--bw-navy</code>,
                    <code>--bw-accent-coral</code>, <code>--grape</code>, <code>--raspberry</code>,
                    <code>--tangelo</code>, <code>--blossom</code>, or any other legacy alias in new code 
                    - they all still resolve, which is exactly the problem: they let old colour names creep
                    back into new components with no warning. Use the semantic tokens (<code>--color-pirmary</code>,
                    <code>--color-secondary</code>, etc.) or the current brand names (<code>--wildfire</code>, 
                    <code>--obsidain</code>...) directly, and treat the aliases as read-only scaffolding for old code, not a menu.
                </p>
            </v-card>
        </v-col>
    </v-row>

     <v-card flat border class="pa-4 mt-6">
        <div class="component-card-title mb-2">Quick reference</div>
        <ul class="rule-list">
            <li><span class=rule-good><v-icon>mdi-check</v-icon></span>Primary actions &amp; emphasis - Wildfire</li>
            <li><span class=rule-good><v-icon>mdi-check</v-icon></span>Secondary accent (was Copper's job) - Merlot</li>
            <li><span class=rule-good><v-icon>mdi-check</v-icon></span>Rosewood pairs with Shadowink or Alabaster text only</li>
            <li><span class=rule-good><v-icon>mdi-check</v-icon></span>One focus treatment, applied consistently everywhere</li>
            <li><span class=rule-bad><v-icon>mdi-close-thick</v-icon></span>Copper - use sparingly</li>
            <li><span class=rule-bad><v-icon>mdi-close-thick</v-icon></span>Obsidian text/elements on Wildfire backgrounds</li>
            <li><span class=rule-bad><v-icon>mdi-close-thick</v-icon></span>Legacy tken aliases ( --bw*, --grape, --raspberry, --tangelo, --blossom) in new code</li>
            <li><span class=rule-bad><v-icon>mdi-close-thick</v-icon></span>--color-primary : --feltboard override in dark mode</li>
        </ul>
    </v-card>

  </section>
</template>

<script setup>
import SectionTitle from '~/components/ui/SectionTitle.vue'
</script>

<style scoped>
.section-border {
  border-top: 1px solid var(--color-border);
}

.group-label {
  font-family: var(--font-display);
  font-size: var(--fs-h4);
  font-weight: var(--fw-bold);
  color: var(--color-secondary);
}
 
.type-demo {
  font-size: var(--fs-h4);
  color: var(--color-text);
  margin: 0;
}

.font-body {
  font-family: var(--font-body);
}

.rule-card {
  border-radius: var(--radius-md);
}
 
.rule-card.do {
  border-color: var(--color-success) !important;
}
 
.rule-card.dont {
  border-color: var(--color-error) !important;
}
 
.rule-card-title {
  font-weight: var(--fw-bold);
}
 
.do-title {
  color: var(--color-success);
}
 
.dont-title {
  color: var(--color-error);
}
 
.rule-demo {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  align-items: center;
}

.demo-chip {
  display: inline-flex;
  align-items: center;
  padding: 6px 14px;
  border-radius: var(--radius-pill);
  font-size: var(--fs-small);
  font-weight: var(--fw-bold);
  color: var(--color-text-inverse);
}

.copper-chip,
.copper-btn {
  background: var(--copper); /* shown only to illustrate what to avoid */
}
 
.wildfire-chip,
.wildfire-btn {
  background: var(--color-primary);
}
 
.demo-block {
  width: 100%;
  padding: 20px;
  border-radius: var(--radius-md);
  font-weight: var(--fw-bold);
  text-align: center;
}
 
.wildfire-obsidian {
  background: var(--wildfire);
  color: var(--obsidian); /* shown only to illustrate what to avoid */
}
 
.wildfire-white {
  background: var(--color-primary);
  color: var(--color-text-inverse);
}
 
.rule-copy {
  font-size: var(--fs-small);
  color: var(--color-text-muted);
}
 
.rule-copy code {
  font-family: ui-monospace, 'SF Mono', Menlo, monospace;
  background: var(--feltboard);
  padding: 1px 6px;
  border-radius: 3px;
  font-size: 0.75rem;
}
 
.component-card-title {
  font-weight: var(--fw-bold);
  color: var(--color-secondary);
}
 
.rule-list {
  list-style: none;
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 0;
  margin: 0;
  font-size: var(--fs-small);
  color: var(--color-text-muted);
}
 
.rule-good {
  color: var(--color-success);
  font-weight: var(--fw-bold);
  margin-right: 6px;
}
 
.rule-bad {
  color: var(--color-error);
  font-weight: var(--fw-bold);
  margin-right: 6px;
}
</style>