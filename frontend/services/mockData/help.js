const createFaq = (question, answer) => ({
    question,
    answer
})

const createTutorial = (
    title,
    description,
    steps,
    route,
    button
) => ({
    title,
    description,
    steps,
    route,
    button
})

const createGuide = (
    id,
    title,
    description,
    icon,
    route,
    topic
) => ({
    id,
    title,
    description,
    icon,
    route,
    topic
})

export const faqs = [
    createFaq(
        'How do I create an account?',
        'Select Sign Up from the navigation bar and complete the registration form.'
    ),
    createFaq(
        'How do I access rulebooks?',
        'Open the Library to browse, search and read available board game rulebooks.'
    ),
    createFaq(
        'How do I join a community?',
        'Navigate to the Community page, choose a community and click Join.'
    ),
    createFaq(
        'How do I create a marketplace listing?',
        'Open the Marketplace and select Create Listing to sell or trade a board game.'
    ),
    createFaq(
        'How do I create an event?',
        'Go to the Events page and select Create Event.'
    ),
    createFaq(
        'How do I discover events?',
        'Browse the Events page to find upcoming board game events near you.'
    ),
    createFaq(
        'How do I manage my game library?',
        'Search for board games and add them to your personal collection from their game pages.'
    ),
    createFaq(
        'Where can I report a problem or request support?',
        'If you need additional assistance, use the Contact Support section at the bottom of this page.'
    )
]

export const tutorials = [
    createTutorial(
        'Browse Rulebooks',
        'Find and read board game rulebooks in the library.',
        [
            'Open the Library.',
            'Search for a board game.',
            'Select a game from the results.',
            'Open and read the rulebook.'
        ],
        '/library',
        'Open Library'
    ),

    createTutorial(
        'Join a Community',
        'Connect with other players and participate in discussions.',
        [
            'Open the Community page.',
            'Browse available communities.',
            'Select a community that interests you.',
            'Click Join to become a member.'
        ],
        '/community',
        'Explore Communities'
    ),

    createTutorial(
        'Game marketplace',
        'Create listings or browse games available from other players.',
        [
            'Open the Marketplace.',
            'Browse listings or search for a game.',
            'Create your own listing or contact a seller.',
            'Complete your transaction.'
        ],
        '/marketplace',
        'Open Marketplace'
    ),

    createTutorial(
        'Discover Events',
        'Find and create board game events in your community.',
        [
            'Open the Events page.',
            'Browse upcoming events.',
            'Join an event or create your own.',
            'Track your attendance.'
        ],
        '/events',
        'View Events'
    )
]

export const popularGuides = [
    createGuide(
    'user-guide',
    'User guide',
    'Open the Boardwise user manual for step-by-step guidance.',
    'mdi-file-document-outline',
    '/docs/Boardwise-User-Manual.pdf',
    'general'
    ),
        createGuide(
    'join-community',
    'Join a community',
    'Find communities and meet other players.',
    'mdi-account-group-outline',
    '/community',
    'communities'
    ),

    createGuide(
    'chats',
    'Chat with players',
    'Message friends and communities.',
    'mdi-message-text-outline',
    '/chats',
    'social'
    ),

    createGuide(
    'invite-friends',
    'Find & invite friends',
    'Find players and send friend invites.',
    'mdi-account-plus-outline',
    '/search',
    'social'
    ),

    createGuide(
    'find-rulebook',
    'Find a rulebook',
    'Search and read game rules.',
    'mdi-book-open-page-variant-outline',
    '/library',
    'library'
    ),

    createGuide(
    'discover-event',
    'Find a game night',
    'Discover upcoming game nights.',
    'mdi-calendar-outline',
    '/events',
    'events'
    ),

    createGuide(
    'marketplace',
    'Marketplace',
    'Browse games listed by players.',
    'mdi-storefront-outline',
    '/marketplace',
    'marketplace'
    )
]