<template>
    <v-container class="py-16">
        <v-row align="center" justify="space-between">
            <v-col cols="12" lg="5" class="text-center">
                <v-img
                    src="/images/landing/setupwizard.svg" 
                    max-width="500"
                />
            </v-col>
            <v-col cols="12" lg="6">
                <v-chip
                    color="primary"
                    class="mb-4"
                    >
                    Setup Wizard
                    </v-chip>

                    <h2 class="landing-heading mb-6">
                        Learn the game without reading the rulebook.
                    </h2>

                    <p class="landing-subtitle mb-8">
                        Boardwise transforms long manuals into
                        interactive, step-by-step guidance so your
                        group spends less time reading and more
                        time playing.
                    </p>

                    <v-list bg-color="transparent">
                        <v-list-item prepend-icon="mdi-check-circle">
                            <v-list-item-title>
                                Breaks rulebooks into simple setup tasks
                            </v-list-item-title>
                        </v-list-item>

                        <v-list-item prepend-icon="mdi-check-circle">
                            <v-list-item-title>
                                Ask questions while setting up
                            </v-list-item-title>
                        </v-list-item>

                        <v-list-item prepend-icon="mdi-check-circle">
                            <v-list-item-title>
                                Supports different editions and expansions
                            </v-list-item-title>
                        </v-list-item>
                    </v-list>
            </v-col>
        </v-row>
    </v-container>
</template>