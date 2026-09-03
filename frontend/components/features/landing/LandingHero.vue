<template>
    <section
        class="landing-hero"
        aria-labelledby="landing-hero-title"
        >
    <v-container class="landing-hero__container"
    >
        <v-row align="center" class="landing-hero__layout" >

            <v-col cols="12" md="7"   lg=7 >

                   
                    <h1
                        id="landing-hero-title"
                        class="landing-hero__title"
                    >
                        Play more. Learn faster.<br> 
                    </h1>
                    <p class="landing-hero__tagline">
                        For people who love the table.
                    </p>

                     <div class="landing-hero__content" aria-label="Boardwise experiences">
                        <div class="hero-badges">
                        <span class="hero-badge hero-badge--connect">Connect</span>
                        <span class="hero-badge hero-badge--share">Share</span>
                        <span class="hero-badge hero-badge--play">Play</span> 
                    </div>
                       
                    <p class="landing-hero__description">
                        Access a library of your favourite board games,
                        discover new games, connect with other players,
                        trade or sell games, all in one place.</p>

                    <div class="landing-hero__actions">
                        <BaseButton
                            variant="secondary"
                            @click="router.push('/library')"
                        >
                            Rulebooks
                        </BaseButton>

                        
                        <BaseButton 
                            @click="router.push('/auth/signup')">
                            Get started 
                        </BaseButton>
                    </div>
                </div>
            </v-col>
           <v-col
                    cols="12"
                    md="5"
                    lg="5"
                >
                    <div class="landing-hero__mascot">
                        <div class="landing-hero__speech">
                            <span>
                                Ready for your next game night?
                            </span>
                        </div>

                        <img
                            src="/images/BoarleySide.svg"
                            alt=""
                            aria-hidden="true"
                            class="landing-hero__mascot-image"
                        >
                    </div>
                </v-col>
        </v-row>
    </v-container>
</section>
</template>

<script setup>
import BaseButton from '~/components/ui/BaseButton.vue'
const router = useRouter()

</script>
