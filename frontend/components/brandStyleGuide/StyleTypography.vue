<template>
    <section class="py-8 section-border" id="typography">
        <SectionTitle 
            title="03 - Type System" 
            subtitle="DynaPuff brings playfulness to headers, Plus Jakarta Sans keeps body copy readable, and Comic Neue handles callouts."
        />

        <div class="d-flex flex-column ga-6 mt-4">

            <div class="type-specimen pb-4">
                <span class="type-meta mb-2">H1 - 40px / DynaPuff Bold</span>
                <h1>The quick brown fox jumps over the lazy dog</h1>
            </div>

            <div class="type-specimen pb-4">
                <span class="type-meta mb-2">H2 - 24px / DynaPuff Bold</span>
                <h2>Explore Strategy & Worker Placement Games</h2>
            </div>

            <div class="type-specimen pb-4">
                <span class="type-meta mb-2">H3 - 20px / DynaPuff Bold</span>
                <h3>Upcoming Game Nights & Events</h3>
            </div>

            <div class="type-specimen pb-4">
                <span class="type-meta mb-2">Body Large - 20px / Plus Jakarta Sans</span>
                <p style="font-size: var(--fs-body-lg);">
                    Boardwise connects players through curated board game events, localized collection management, and community reviews.
                </p>
            </div>

            <div class="type-specimen pb-4">
                <span class="type-meta mb-2">Body - 15px / Plus Jakarta Sans</span>
                <p style="font-size: var(--fs-body);">
                    Standard text used across game summaries, rule descriptions, comments, and form inputs. Optimized for legibility.
                </p>
            </div>

            <!-- <div class="type-specimen pb-4"> Still a thought
                <span class="type-meta mb-2">Fun Callout - Comic Neue</span>
                <p style="font-family: var(--font-fun); font-size: 1.2rem; color: var(--color-primary);">
                    "Don't forget to trade 3 wool for 1 brick before ending your turn!"
                </p>
            </div> -->
        </div>
    </section>
</template>

<script setup>
import SectionTitle from '../ui/SectionTitle.vue';
</script>

<style scoped>
.section-border {
  border-top: 1px solid var(--color-border);
}

.type-specimen {
    border-bottom: 1px dashed var(--color-border);
}

.type-meta {
    display: block;
    color: var(--color-primary);
    font-weight: var(--fw-bold);
}
</style>