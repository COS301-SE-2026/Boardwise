<template>
    <section
    id="logo"
    class="py-8 section-border"
>

    <SectionTitle
        title="02 - Logos and Boarley the mascot"
        subtitle="
        The Boardwise logo system represents the identity of the platform.
        Use approved logo variations to maintain consistency across all digital experiences.
        "
    />

    <v-card
        flat
        border
        class="pa-6 mt-6"
    >
        <h3 class="section-title">
            Using Boardwise Brand Assets
        </h3>

        <p class="text-body">
            The Boardwise logo should always represent the brand accurately.
            Never modify, recreate, distort, or alter official logo assets.
        </p>

        <p class="text-body mt-3">
            The logo system includes multiple variations designed for different
            layouts while maintaining a consistent identity.
        </p>

        <h3 class="heading mt-8 mb-4">
        Primary Logos
    </h3>

    <v-row>

        <v-col
            v-for="logo in primaryLogos"
            :key="logo.name"
            cols="12"
            md="6"
        >

            <v-card
                flat
                border
                class="logo-card pa-6"
            >

                <v-img
                    :src="logo.src"
                    height="120"
                    contain
                    class="mb-5"
                />

                <h4>
                    {{ logo.name }}
                </h4>


                <p class="description">
                    {{ logo.description }}
                </p>


                <v-chip
                    size="small"
                    color="primary"
                    variant="flat"
                >
                    {{ logo.usage }}
                </v-chip>


            </v-card>

        </v-col>

    </v-row>
    </v-card>
    <v-card
  flat
  border
  class="pa-6 mt-8"
>
  <h3 class="heading mb-6">
    Logo Usage Guidelines
  </h3>

  <v-row>

    <v-col cols="12" md="6">
      <h4 class="do mb-4">
        ✓ Do
      </h4>

      <v-card
        v-for="item in dos"
        :key="item.title"
        flat
        border
        class="usage-card mb-4 pa-4"
      >
        <v-img
          :src="item.image"
          height="140"
          contain
          class="mb-4"
        />

        <h5 class="usage-title">
          {{ item.title }}
        </h5>

        <p class="usage-description">
          {{ item.description }}
        </p>
      </v-card>
    </v-col>

    <v-col cols="12" md="6">
      <h4 class="dont mb-4">
        ✕ Don't
      </h4>

      <v-card
        v-for="item in donts"
        :key="item.title"
        flat
        border
        class="usage-card mb-4 pa-4"
      >
        <v-img
          :src="item.image"
          height="140"
          contain
          class="mb-4"
        />

        <h5 class="usage-title">
          {{ item.title }}
        </h5>

        <p class="usage-description">
          {{ item.description }}
        </p>
      </v-card>
    </v-col>

  </v-row>

</v-card>
</section>
</template>

<script setup>
import SectionTitle from '../ui/SectionTitle.vue'

const primaryLogos = [
    {
    name:'Light Mode Logo',
    src:'/images/main.svg',
    usage:'Usage',
    description:
    'The main Boardwise logo used for websites, navigation, documentation and wide layouts.'
},
    {
    name:'Dark Mode Logo',
    src:'/images/pink.svg',
    usage:'Primary Usage',
    description:
    'The main Boardwise logo used for websites, navigation, documentation and wide layouts.'
},


{
    name:'Icon Logo',
    src:'/images/Boarley.svg',
    usage:'Compact Layouts',
    description:
    'Designed for smaller spaces including mobile layouts, social profiles.'
}
]

const dos = [

{
title:'Use approved logo assets',
image:'/images/stacked.svg',
description:
'Always use the official Boardwise logo files without modification.'
},


{
title:'Maintain clear space',
image:'/images/horizontal.svg',
description:
'Keep enough empty space around the logo so it remains visible.'
},


{
title:'Use correct proportions',
image:'/images/Boarley.svg',
description:
'Scale the logo proportionally without changing its shape.'
}


]




const donts = [

{
title:'Do not stretch the logo ',
image:'/images/B_horizontal.svg',
description:
'Never change the width or height independently.'
},


{
title:'Do not change colours',
image:'/images/BOARDWISE.svg',
description:
'Do not apply custom colours, gradients or effects.'
},


{
title:'Do not rotate the logo',
image:'/images/rotate.svg',
description:
'The logo should always remain upright.'
}

]
</script>