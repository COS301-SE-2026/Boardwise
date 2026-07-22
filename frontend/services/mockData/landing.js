export const onboardingSteps = [
    {
        id: 'i',
        title: "Create your account",
        description: "Sign up and create your Boardwise profile.",
        icon: "mdi-account-plus-outline",
        route: "/auth/signup"
    },
    {
        id: 'ii',
        title: 'Build your collection',
        step: 'Build Your Library',
        description: "Add the board games you own or want to play.",
        icon: "mdi-bookshelf",
        route: "/vault"
    },
    {
        id: 'iii',
        title: 'Join the community',
        description: "Meet other players and join discussions.",
        icon: "mdi-account-group-outline",
        route: "/community"
    },
    {
        id: 'iv',
        title: 'Discover games',
        description: "Explore the library and find your next favourite game.",
        icon: "mdi-compass-outline",
        route: "/library"
    },
    {
        id: 'v',
        title: 'Trade with others',
        description: "Buy, sell and trade board games securely.",
        icon: "mdi-swap-horizontal-bold",
        route: "/marketplace"
        },
]

export const platformFeatures = [
    {
        id: 1,
        title: 'Library',
        description: "Browse board games, digital rulebooks and build your collection.",
        icon: "mdi-bookshelf",
        route: "/library"
    },
    {
        id: 2,
        title: 'Marketplace',
        description: "Search the maket and trade board games with other players.",
        icon: "mdi-storefront-outline",
        route: "/marketplace"
        
    },
    {
        id: 3,
        title: 'Community',
        description: "Join discussions and connect with fellow board gamers.",
        icon: "mdi-account-group-outline",
        route: "/community"
    },
    {
        id: 4,
        title: 'Events',
        description: "Share invites and local board game events.",
        icon: "mdi-calendar-star",
        route: "/events"
  }
]