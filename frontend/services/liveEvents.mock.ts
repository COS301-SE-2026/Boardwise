export const mockLiveEvents = [
    {
        id: 'live-1',
        name: 'Monopoly Marathon',
        game: 'Monopoly',
        venue: 'Tabletop Tavern Hatfield',
        table: 'Table 4',
        status: 'LIVE',
        capacity: 8,
        seats: [
        { seat: 1, user: { username: 'IAmR3al' }, isHost: true, status: 'SEATED' },
        { seat: 2, user: { username: 'Sarah_BGG' }, status: 'SEATED' },
        { seat: 3, user: { username: 'MarcusK' }, status: 'SEATED' },
        { seat: 4, user: { username: 'DevonCole' }, status: 'EN_ROUTE' },
        { seat: 5, user: { username: 'SamiraT' }, status: 'SEATED' },
        { seat: 6, user: { username: 'AlexV' }, status: 'SEATED' },
        { seat: 7, user: { username: 'TL_Gamer' }, status: 'SEATED' },
        { seat: 8, user: null, status: 'OPEN' }
        ],
        startedAt: Date.now() - (1 * 3600 + 24 * 60) * 1000,
        messages: [
        { id: 'm1', user: 'IAmR3al', isHost: true, text: 'Grabbed Table 4, drinks at the front bar!', ts: Date.now() - 1000 * 60 * 10 }
        ]
    }
]