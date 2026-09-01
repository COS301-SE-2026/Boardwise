export const faqs = [
    {
        question: 'How do I create an account?',
        answer: 'Select Sign Up from the navigation bar and complete the registration form.'
    },
    {
        question: 'How do I access rulebooks?',
        answer: 'Open the Library to browse, search and read available board game rulebooks.'
    },
    {
        question: 'How do I join a community?',
        answer: 'Navigate to the Community page, choose a community and click Join.'
    },
    {
        question: 'How do I create a marketplace listing?',
        answer: 'Open the Marketplace and select Create Listing to sell or trade a board game.'
    },
    {
        question: 'How do I create an event?',
        answer: 'Go to the Events page and select Create Event.'
    },
    {
        question: 'How do I discover  events?',
        answer: 'Browse the Events page to find upcoming board game events near you.'
    },
    {
        question: 'How do I manage my game library?',
        answer: 'Search for board games and add them to your personal collection from their game pages.'
    },
    {
    question: 'Where can I report a problem or request support?',
    answer: 'If you need additional assistance, use the Contact Support section at the bottom of this page.'
    }   
]

export const tutorials = [
    {
        title: 'Browse Rulebooks',
        description: 'Find and read board game rulebooks in the library.',
        steps: [
            'Open the Library.',
            'Search for a board game.',
            'Select a game from the results.',
            'Open and read the rulebook.'
        ],
        route: '/library',
        button: 'Open Library'
    },
    {
        title: 'Join a Community',
        description: 'Connect with other players and participate in discussions.',
        steps: [
            'Open the Community page.',
            'Browse available communities.',
            'Select a community that interests you.',
            'Click Join to become a member.'
        ],
        route: '/community',
        button: 'Explore Communities'
    },
    {
        title: 'Game marketplace',
        description: 'Create listings or browse games available from other players.',
        steps: [
            'Open the Marketplace.',
            'Browse listings or search for a game.',
            'Create your own listing or contact a seller.',
            'Complete your transaction.'
        ],
        route: '/marketplace',
        button: 'Open Marketplace'
    },
    {
        title: 'Discover Events',
        description: 'Find and create board game events in your community.',
        steps: [
            'Open the Events page.',
            'Browse upcoming events.',
            'Join an event or create your own.',
            'Track your attendance.'
        ],
        route: '/events',
        button: 'View Events'
    }
]

export const popularGuides = [
    {
        id: 'join-community',
        title: 'Join a community',
        description: 'Find a community and connect with other board game players.',
        icon: 'mdi-account-group-outline',
        route: '/community',
        topic: 'communities'
    },
    {
        id: 'find-rulebook',
        title: 'Find a rulebook',
        description: 'Search the Library and open the rules for a game.',
        icon: 'mdi-book-open-page-variant-outline',
        route: '/library',
        topic: 'library'
    },
    {
        id: 'discover-event',
        title: 'Find a game night',
        description: 'Browse upcoming events and find something to join.',
        icon: 'mdi-calendar-outline',
        route: '/events',
        topic: 'events'
    },
    {
        id: 'marketplace',
        title: 'Use the Marketplace',
        description: 'Browse board games being sold by the Boardwise community.',
        icon: 'mdi-storefront-outline',
        route: '/marketplace',
        topic: 'marketplace'
    },
    {
        id: 'privacy',
        title: 'Manage your privacy',
        description: 'Control who can see your Boardwise profile and activity.',
        icon: 'mdi-shield-account-outline',
        route: '/settings',
        topic: 'account'
    }
]