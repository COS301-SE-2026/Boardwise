<!-- Base Card for every other Card (Marketplace, Library and Profile Cards) -->
<template> 
    <v-card 
        v-card 
        rounded="lg"
        :elevation="0"
        border
        v-bind="$attrs"
    >

        <slot />
    </v-card>
</template>