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
    {
        id: 'vi',
        title: 'Track your collection',
        description: "Keep your collection organised in your vault.",
        icon: "mdi-safe-square-outline",
        route: "/vault"
    }
]

export const platformFeatures = [
    {
        id: 1,
        title: 'Library',
        description: "Browse thousands of board games, digital rulebooks and build your collection.",
        icon: "mdi-bookshelf",
        route: "/library"
    },
    {
        id: 2,
        title: 'Community',
        description: "Buy, sell and trade board games safely with other players.",
        icon: "mdi-storefront-outline",
        route: "/marketplace"
        
    },
    {
        id: 3,
        title: 'Marketplace',
        description: "Join discussions, discover events and connect with board gamers.",
        icon: "mdi-account-group-outline",
        route: "/community"
    },
    {
        id: 4,
        title: 'Vault',
        description: "Organise your collection and keep your favourite games in one place.",
        icon: "mdi-safe-square-outline",
        route: "/profile"
    },
    {
        id: 5,
        title: 'Events',
        description: "Find tournaments, meetups and local board game events.",
        icon: "mdi-calendar-star",
        route: "/events"
  },
  {
        id: 6,
        title: 'Discover',
        description: "Receive personalised recommendations based on your interests.",
        icon: "mdi-compass-outline",
        route: "/library"
    }
]