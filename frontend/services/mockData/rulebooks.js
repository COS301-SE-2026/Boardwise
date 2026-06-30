export const rulebooks = [
  {
    id: 1,
    title: 'Catan',
    genre: 'Strategy',
    image: '/images/catan.jpg',
    description: 'Trade resources and build settlements on the island of Catan. Collect and trade resources like wood, brick, wheat, ore and sheep to build roads, settlements and cities.',
    players: '3-4',
    duration: '60-120 min',
    age: '10+',
    difficulty: 'Medium',
    pages: [
      {
        title: 'Overview',
        content: `Catan is a multiplayer board game in which players collect resources and use them to build roads, settlements and cities on their way to dominating the island of Catan. Players collect resources — wood, brick, wheat, ore and sheep — by rolling dice each turn. Players then spend their resources to build roads, settlements and cities, or buy development cards. The first player to reach 10 victory points wins the game.`
      },
      {
        title: 'Setup',
        content: `Place the hex tiles randomly to form the island. Each hex is marked with a number token. Shuffle the port pieces and place them around the board. Each player chooses a color and takes the corresponding pieces: 5 settlements, 4 cities, and 15 roads. Each player places 2 starting settlements and 2 roads on the board. Distribute starting resources based on the second settlement placement.`
      },
      {
        title: 'On Your Turn',
        content: `Each turn consists of three phases. First, roll both dice. All players with settlements adjacent to hexes matching the rolled number collect the corresponding resources. Second, trade resources with other players or the bank. Third, build roads, settlements, cities or buy development cards using your resources. Roads cost 1 wood and 1 brick. Settlements cost 1 wood, 1 brick, 1 wheat and 1 sheep. Cities cost 2 wheat and 3 ore. Development cards cost 1 ore, 1 wheat and 1 sheep.`
      },
      {
        title: 'The Robber',
        content: `When a 7 is rolled, no resources are produced. The player who rolled must move the robber to a new hex. Any player with more than 7 resource cards must discard half. The player who moved the robber may steal one resource card from any player with a settlement adjacent to the robber's new location.`
      },
      {
        title: 'Victory Points',
        content: `Players earn victory points for settlements (1 point each), cities (2 points each), the Longest Road card (2 points), the Largest Army card (2 points), and certain development cards. The first player to reach 10 victory points on their turn wins immediately.`
      }
    ]
  },
  {
    id: 2,
    title: 'Wingspan',
    genre: 'Engine Builder',
    image: '/images/wingspan.jpg',
    description: 'Attract birds to your wildlife preserve in this engine-building game. Each bird extends a chain of actions in one of three habitats.',
    players: '1-5',
    duration: '40-70 min',
    age: '10+',
    difficulty: 'Medium',
    pages: [
      {
        title: 'Overview',
        content: `Wingspan is a competitive bird-collection engine-building game. Players are bird enthusiasts seeking to attract the best birds to their wildlife preserves. Each bird you play extends a chain of powerful actions in one of your three habitats. The goal is to have the most points after four rounds.`
      },
      {
        title: 'Setup',
        content: `Each player receives a player mat, 5 random bird cards, 5 food tokens (one of each type) and 8 eggs. Remove 2 bird cards from the game and place the remaining cards as the draw deck. Set up the bird feeder dice tray with all 5 dice. Place the round end goal tiles on the goal board. Each player discards down to any combination of 2 bird cards and food tokens.`
      },
      {
        title: 'Your Turn',
        content: `On your turn, choose one of four actions. Play a bird from your hand onto your mat by paying its food and egg cost. Gain food by rolling the dice in the bird feeder. Lay eggs on your birds. Draw bird cards from the deck or display. You may only take one action per turn.`
      },
      {
        title: 'Habitats',
        content: `The forest helps you gain food. The grassland helps you lay eggs. The wetland helps you draw cards. Each habitat has a row on your player mat. As you add birds to a habitat, the actions in that row become more powerful. Each new bird played activates the powers of birds already in that row from right to left.`
      },
      {
        title: 'Scoring',
        content: `After four rounds, players tally their scores. Points come from birds played, bonus cards, end-of-round goals, eggs on birds, food tokens cached on birds, and tucked cards. The player with the most points wins.`
      }
    ]
  },
  {
    id: 3,
    title: 'Ticket To Ride',
    genre: 'Family',
    image: '/images/ticket.jpg',
    description: 'Build railway routes across countries to connect cities and complete destination tickets.',
    players: '2-5',
    duration: '30-90 min',
    age: '8+',
    difficulty: 'Easy',
    pages: [
      {
        title: 'Overview',
        content: `Ticket to Ride is a cross-country train adventure game. Players collect train cards to claim railway routes connecting cities across the map. The longer the routes, the more points they earn. Additional points come from completing destination tickets connecting distant cities, and to the player who builds the longest continuous railway.`
      },
      {
        title: 'Setup',
        content: `Lay out the board in the center of the table. Each player takes 45 colored train pieces and the matching scoring marker. Shuffle the destination tickets and deal 3 to each player — each player must keep at least 2. Deal 4 train cards face up as the card display and place the remaining deck nearby. Each player starts with 4 train cards.`
      },
      {
        title: 'On Your Turn',
        content: `Each turn take one of three actions. Draw two train cards from the face-up display or the deck. Claim a route by playing matching colored train cards equal to the route's length and placing your train pieces on it. Draw three destination tickets and keep at least one. Claimed routes score points immediately based on their length.`
      },
      {
        title: 'Claiming Routes',
        content: `To claim a route, play a set of matching colored train cards equal to the length of that route. Grey routes can be claimed using any single color. Double routes between cities can only both be claimed in games with 4 or more players. Once claimed, a route cannot be taken by another player.`
      },
      {
        title: 'End of Game',
        content: `The game ends when any player has 2 or fewer train pieces remaining. Each player takes one final turn. Then players reveal their destination tickets. Completed tickets add their point value; incomplete ones subtract it. The player with the longest continuous route earns 10 bonus points. The player with the most points wins.`
      }
    ]
  }
]