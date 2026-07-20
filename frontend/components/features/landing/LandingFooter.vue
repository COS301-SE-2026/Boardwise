<template>
    <v-footer class="py-8 px-6">
        <v-container>
            <v-row>
                <v-col cols="12" md="4">
                    <h3 class="text-h6 font-weight-bold">
                        Boardwise
                    </h3>
                    <p class="text-body-2 text-medium-emphasis mt-2">
                        Connect · Share · Play
                    </p>

                    <p class="text-body-2 mt-4">
                        Discover games, learn the rules faster, and connect with players through one intelligent platform.
                    </p>
                </v-col>

                <v-col cols="6" md="2">
                    <h4 class="text-subtitle-1 font-weight-bold mb-3">
                        Explore
                    </h4>
                    
                    <div class="d-flex flex-column ga-2">
                        <NuxtLink to="/library">Library</NuxtLink>
                        <NuxtLink to="/marketplace">Marketplace</NuxtLink>
                        <NuxtLink to="/community">Communities</NuxtLink>
                    </div>
                </v-col>

                <v-col>
                    <h4 class="text-subtitle-1 font-weight-bold mb-3">
                        Account
                    </h4>

                    <div class="d-flex flex-column ga-2">
                        <NuxtLink to="/auth/signup">Sign in</NuxtLink>
                        <NuxtLink to="/auth/signup">Register</NuxtLink>
                    </div>
                </v-col>

                <v-col cols="12" md="4">
                    <h4 class="text-subtitle-1 font-weight-bold mb-3">
                        Powered by 
                    </h4>

                    <a
                        href="https://boardgamegeek.com"
                        target="_blank"
                        rel="noopener noreferrer"
                        class="text-decoration-none"
                    >
                        <v-img
                            src="/images/landing/powered-by-bgg-rgb.svg"
                            max-width="220"
                            contain
                            alt="Powered by BoardGameGeek"
                        />
                    </a>

                    <p class="text-body-2 text-medium-emphasis mt-3">
                        Board game information and metadata provided by
                        BoardGameGeek.
                    </p>
                </v-col>
            </v-row>

            <v-divider class="my-6"/>

            <div class="text-center text-body-2 text-medium-emphasis">
                Boardwise. All rights reserved to Epi-Use and WorksOnMyMachine.
            </div>
        </v-container>
    </v-footer>
</template>