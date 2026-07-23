<template>
    <v-app>
        <StyleNav :is-dark="isDark" @toggle-theme="toggleTheme" />

        <v-main>
            <v-container class="max-width-container">
                <StyleHero />
            </v-container>

            <footer class="py-8 section-border text-center">
                <v-container>
                <div class="footer-brand mb-1">Boardwise</div>
                <div class="text-caption text-grey">Brand Design System v2.0</div>
                </v-container>
            </footer>

        </v-main>
    </v-app>
</template>

<script setup>
import StyleHero from '~/components/brandStyleGuide/StyleHero.vue';
import StyleNav from '~/components/brandStyleGuide/StyleNav.vue';

</script>