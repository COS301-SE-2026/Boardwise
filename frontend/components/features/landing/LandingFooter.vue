<template>
    <v-footer class=" py-10">
        <v-container>
            <v-row>
                <v-col cols="12" md="4">
                    <h3>
                        Boardwise
                    </h3>
                    <p class="mt-4">
                          Discover, collect and share board games with the Boardwise community.
                    </p>
                </v-col>

                
                    <div>
                        <span class="hero-badge hero-badge--connect">Connect</span>
                        <span class="hero-badge hero-badge--share">Share</span>
                        <span class="hero-badge hero-badge--play">Play</span>
                    </div>

                <v-col cols="6" md="2">
                    <h4 >
                        Explore
                    </h4>
                    
                    <div class="d-flex flex-column ga-2">
                        <NuxtLink to="/library"
                            class="text-decoration-none text-medium-emphasis"
                            >Library
                        </NuxtLink>
                        <NuxtLink to="/marketplace"
                            class="text-decoration-none text-medium-emphasis"
                            >Marketplace
                            
                        </NuxtLink>
                        <NuxtLink to="/community"
                            class="text-decoration-none text-medium-emphasis"
                        >Communities

                        </NuxtLink>
                        <NuxtLink to="/events"
                            class="text-decoration-none text-medium-emphasis"
                        >Events

                        </NuxtLink>
                        <NuxtLink to="/help"
                            class="text-decoration-none text-medium-emphasis"
                        >Help

                        </NuxtLink>
                    </div>
                </v-col>

                <v-col>
                    <h4 >
                        Account
                    </h4>

                    <div class="d-flex flex-column ga-2">
                        <NuxtLink to="/auth/signup">Sign In</NuxtLink>
                        <NuxtLink to="/auth/signup">Sign Up</NuxtLink>
                    </div>
                </v-col>

                <v-col cols="12" md="4">
                    
                    <a
                        href="https://boardgamegeek.com"
                        target="_blank"
                        rel="noopener noreferrer"
                        class="d-inline-block mt-4"
                    >
                        <v-img
                            src="/images/powered-by-bgg-rgb.svg"
                            alt="Powered by BoardGameGeek"
                            style="width:180px; display:block;"
                        />
                    </a>

                </v-col>
            </v-row>

        </v-container>
    </v-footer>
</template>