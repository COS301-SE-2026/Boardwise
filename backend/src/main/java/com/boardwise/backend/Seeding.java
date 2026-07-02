package com.boardwise.backend;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bson.types.ObjectId;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import com.boardwise.backend.marketplace.enums.ListingStatus;
import com.boardwise.backend.marketplace.model.Listing;
import com.boardwise.backend.marketplace.model.RentalPeriod;
import com.boardwise.backend.marketplace.repository.ListingRepository;
import com.boardwise.backend.user_service.models.Boardgame;
import com.boardwise.backend.user_service.models.Group;
import com.boardwise.backend.user_service.models.GroupMembership;
import com.boardwise.backend.user_service.models.User;
import com.boardwise.backend.user_service.repos.BoardGameRepository;
import com.boardwise.backend.user_service.repos.GroupMembershipRepository;
import com.boardwise.backend.user_service.repos.GroupRepository;
import com.boardwise.backend.user_service.repos.UserRepository;
import com.boardwise.backend.vault.model.Chunk;
import com.boardwise.backend.vault.model.EditEvent;
import com.boardwise.backend.vault.model.IngestionJob;
import com.boardwise.backend.vault.model.Rulebook;
import com.boardwise.backend.vault.model.RulebookText;
// import com.boardwise.backend.vault.model.WriteLock;
import com.boardwise.backend.vault.repository.EditEventRepository;
import com.boardwise.backend.vault.repository.IngestionJobRepository;
import com.boardwise.backend.vault.repository.RulebookRepository;
import com.boardwise.backend.vault.repository.RulebookTextRepository;
import com.boardwise.backend.vault.repository.WriteLockRepository;
import com.boardwise.backend.marketplace.enums.Genres;

@Component
@Profile("!test")
public class Seeding {

    private ObjectId getObjectIdFromUsername(String username, UserRepository userRepository) {
            return new ObjectId(userRepository.findByUsername(username).get().getId());
    }

        
    @Bean
    public CommandLineRunner seedDB(ListingRepository listingRepository, BoardGameRepository boardGameRepository, GroupMembershipRepository groupMembershipRepository,
            GroupRepository groupRepository, UserRepository userRepository, EditEventRepository editEventRepository,
            IngestionJobRepository ingestionJobRepository, RulebookRepository rulebookRepository, RulebookTextRepository rulebookTextRepository,
            WriteLockRepository writeLockRepository) {
        return args -> {
            // User Repository
            if (userRepository.count() == 0) {
                BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
                List<User> users = List.of(
                        new User("IAmR3al", "John", "Doe", "johnsemail@test.com", encoder.encode("J0hnDo3_")),
                        new User("bob", "Bob", "Smith", "bob.smith@example.com", encoder.encode("MyP@ssw0rd!")),
                        new User("jane_doe", "Jane", "Doe", "jane.doe@company.co.uk", encoder.encode("C0mpl3x!P@ss#2024")),
                        new User("sarah_dev", "Sarah", "Chen", "sarah.developer@techstartup.io", encoder.encode("K8$mPx2@vLq9")),
                        new User("alex_games", "Alex", "Turner", "alex.turner@gmail.com", encoder.encode("G@m3rAl3x#99")),
                        new User("mike_b", "Michael", "Brown", "michael.brown@outlook.com", encoder.encode("Br0wn!Mike_7")),
                        new User("lena_play", "Lena", "Visser", "lena.visser@gmail.com", encoder.encode("L3na!V1ss3r#")),
                        new User("thandeka_m", "Thandeka", "Mokoena", "thandeka.mokoena@outlook.com", encoder.encode("Th@nd3k@M0k!")),
                        new User("ruan_sa", "Ruan", "Pieterse", "ruan.pieterse@webmail.co.za", encoder.encode("Ru@nP13t3rs3")),
                        new User("gamer_kyle", "Kyle", "Watson", "kyle.watson@gmail.com", encoder.encode("Kyl3W@ts0n!!")),
                        new User("priya_rolls", "Priya", "Naidoo", "priya.naidoo@techmail.co.za", encoder.encode("Pr1y@N@1d00#")),
                        new User("deon_dice", "Deon", "van der Merwe", "deon.vdm@mweb.co.za", encoder.encode("D3onD!c3_99")),
                        new User("zoe_tiles", "Zoe", "Khumalo", "zoe.khumalo@gmail.com", encoder.encode("Z03T1l3s@22")),
                        new User("marco_strat", "Marco", "Ferreira", "marco.ferreira@sapo.pt", encoder.encode("M@rc0Fr3rr@!")),
                        new User("amber_quest", "Amber", "Jacobs", "amber.jacobs@yahoo.com", encoder.encode("@mb3rJ@c0bs#")),
                        new User("sipho_board", "Sipho", "Dlamini", "sipho.dlamini@telkomsa.net", encoder.encode("S1ph0Dl@m1n!")));
                userRepository.saveAll(users);
                System.out.println("Seeded " + users.size() + " users");
            } else {
                System.out.println("Users already seeded, skipping...");
            }

            // Listing
            if (listingRepository.count() == 0) {

                RentalPeriod rentalPeriod1 = new RentalPeriod();
                rentalPeriod1.setStartDate(LocalDate.of(2026, 7, 16));
                rentalPeriod1.setEndDate(LocalDate.of(2026, 9, 15));

                RentalPeriod rentalPeriod2 = new RentalPeriod();
                rentalPeriod2.setStartDate(LocalDate.of(2026, 7, 1));
                rentalPeriod2.setEndDate(LocalDate.of(2026, 10, 1));

                RentalPeriod rp2 = new RentalPeriod();
                rp2.setStartDate(LocalDate.of(2026, 7, 1));
                rp2.setEndDate(LocalDate.of(2026, 10, 1));

                RentalPeriod rp3 = new RentalPeriod();
                rp3.setStartDate(LocalDate.of(2026, 8, 1));
                rp3.setEndDate(LocalDate.of(2026, 8, 31));

                RentalPeriod rp4 = new RentalPeriod();
                rp4.setStartDate(LocalDate.of(2026, 7, 10));
                rp4.setEndDate(LocalDate.of(2026, 9, 10));

                RentalPeriod rp5 = new RentalPeriod();
                rp5.setStartDate(LocalDate.of(2026, 6, 15));
                rp5.setEndDate(LocalDate.of(2026, 8, 15));

                List<Listing> listings = List.of(
                        new Listing(null, "IAmR3al", getObjectIdFromUsername("IAmR3al", userRepository) , "full boardgame", "sale", 29.99,"Pretoria",true,"Monopoly board game for sale", "new" ,"Monopoly","millionaire e.d.",
                                "Monopoly game with all details\n", "https://pub-c543dd80255b4b9c9c31a54e09389b5d.r2.dev/listings/Monopoly/Monopoly.png",
                                ListingStatus.AVAILABLE, LocalDateTime.now(), LocalDateTime.now(),
                                List.of(Genres.STRATEGY.getValue(), Genres.ACTION_DEXTERITY.getValue()), null),
                        
                        new Listing(null, "sarah_dev",getObjectIdFromUsername("sarah_dev", userRepository), "pieces", "rental", 48.32,"Johannesburg", false,"Some pieces","new", "Scrabble","base",
                                "game of scrabble pieces", "https://pub-c543dd80255b4b9c9c31a54e09389b5d.r2.dev/listings/Scrabble/Scrabble.jpg",
                                ListingStatus.AVAILABLE, LocalDateTime.now().plusDays(5),
                                LocalDateTime.now().plusDays(5), List.of("abstract strategy"), rentalPeriod1),
                        
                        new Listing(null, "bob", getObjectIdFromUsername("bob", userRepository),
                                "partial boardgame", "sale", 350.00, "Cape Town",true,"2nd hand Catan game ", "new", "Catan","base",
                                "Settlers of Catan 5th edition. Complete with all expansions. Excellent condition.",
                                "/images/catan.jpg",
                                ListingStatus.AVAILABLE, LocalDateTime.now(), LocalDateTime.now(),
                                List.of(Genres.STRATEGY.getValue(), Genres.NEGOTIATION.getValue(), Genres.ECONOMIC.getValue()), null),

                        new Listing(null, "jane_doe", getObjectIdFromUsername("jane_doe", userRepository),
                                "full boardgame", "sale", 420.00, "Durban",true,"Ticker to Ride board game from my childhood", "good", "Ticket to Ride","base",
                                "Ticket to Ride original edition. All cards and train pieces present. Box slightly worn.",
                                "/images/ticket.jpg",
                                ListingStatus.AVAILABLE, LocalDateTime.now(), LocalDateTime.now(),
                                List.of(Genres.STRATEGY.getValue(), Genres.TRAINS.getValue(), Genres.TRANSPORTATION.getValue()), null),

                        new Listing(null, "alex_games", getObjectIdFromUsername("alex_games", userRepository),
                                "full boardgame", "rental", 55.00, "Johannesburg",false, "Idk what Azul is but its going for cheap","fair", "Azul","base",
                                "Azul tile-drafting game. Perfect condition, all tiles accounted for. Great for 2–4 players.",
                                "/images/azul.jpg",
                                ListingStatus.AVAILABLE, LocalDateTime.now(), LocalDateTime.now(),
                                List.of(Genres.ABSTRACT_STRATEGY.getValue(), Genres.PUZZLE.getValue()), rp2),

                        new Listing(null, "mike_b", getObjectIdFromUsername("mike_b", userRepository),
                                "full boardgame", "sale", 280.00, "Pretoria", false,"Dixit game set with all pieces, DM me", "fair", "Dixit","base",
                                "Dixit base game with all 84 cards. Wonderful storytelling game for families.",
                                "/images/dixit.jpg",
                                ListingStatus.AVAILABLE, LocalDateTime.now(), LocalDateTime.now(),
                                List.of(Genres.PARTY_GAME.getValue(), Genres.HUMOR.getValue()), null),

                        new Listing(null, "lena_play", getObjectIdFromUsername("lena_play", userRepository),
                                "partial boardgame", "sale", 310.00, "Stellenbosch",true,"wingspan board game with a few missing pieces","like new",  "Wingspan","base",
                                "Wingspan with Oceania expansion included. Lightly played, missing components intact.",
                                "/images/wingspan.jpg",
                                ListingStatus.AVAILABLE, LocalDateTime.now(), LocalDateTime.now(),
                                List.of(Genres.STRATEGY.getValue(), Genres.ANIMALS.getValue(), Genres.FARMING.getValue()), null),

                        new Listing(null, "thandeka_m", getObjectIdFromUsername("thandeka_m", userRepository),
                                "asset", "rental", 60.00, "Johannesburg",true,"GOT board game assets, for rent", "like new", "Game of Thrones","base",
                                "Game of Thrones board game 2nd edition. Supports up to 6 players. Epic political strategy.",
                                "/images/gameofthrones.png",
                                ListingStatus.AVAILABLE, LocalDateTime.now(), LocalDateTime.now(),
                                List.of(Genres.STRATEGY.getValue(), Genres.NEGOTIATION.getValue(), Genres.POLITICAL.getValue(), Genres.FANTASY.getValue()), rp3),

                        new Listing(null, "ruan_sa", getObjectIdFromUsername("ruan_sa", userRepository),
                                "partial boardgame", "sale", 195.00, "Centurion", false,"","like new","Kingdom Builder","base",
                                "Kingdom Builder base game. Complete, all tiles and tokens present. Minor box shelf wear.",
                                "/images/kingdom.png",
                                ListingStatus.AVAILABLE, LocalDateTime.now(), LocalDateTime.now(),
                                List.of(Genres.STRATEGY.getValue(), Genres.TERRITORY_BUILDING.getValue()), null),

                        new Listing(null, "zoe_tiles", getObjectIdFromUsername("zoe_tiles", userRepository),"merch","sale",650,"Braam",true,"Custom Monopoly hoodie", "fair","Monopoly","unknown",
                                "2XL Monopoly man hoodie","/images/MonopolyManHoodie",ListingStatus.AVAILABLE,LocalDateTime.now(),LocalDateTime.now(), List.of(Genres.STRATEGY.getValue(),Genres.DICE.getValue()),null));
                                
                listingRepository.saveAll(listings);
                System.out.println("Seeded " + listings.size() + " listings");
            } else {
                System.out.println("Listings already seeded, skipping...");
            }

            if (boardGameRepository.count() == 0) {
                List<Boardgame> boardGames = List.of(
                        new Boardgame(null, 1 ,"Monopoly", "Classic property trading game.",
                                "https://pub-c543dd80255b4b9c9c31a54e09389b5d.r2.dev/listings/Monopoly/Monopoly.png", 2, 8, 
                                List.of("Strategy", "Trading")),
                        new Boardgame(null, 2,"Scrabble", "Word building board game.",
                                "https://pub-c543dd80255b4b9c9c31a54e09389b5d.r2.dev/listings/Scrabble/Scrabble.jpg", 2, 4, 
                                List.of("Word", "Abstract Strategy")),
                        new Boardgame(null,13,"Catan","Resource trading and settlement building game."
                                ,"https://cf.geekdo-images.com/0XODRpReiZBFUffEcqT5-Q__imagepage/img/enC7UTvCAnb6j1Uazvh0OBQjvxw=/fit-in/900x600/filters:no_upscale():strip_icc()/pic9156909.png",3,4,
                                List.of("Strategy", "Negotiation", "Economic")),
                        new Boardgame(null,20549,"Pandemic","Cooperative game to cure global disease",
                                "https://cf.geekdo-images.com/S3ybV1LAp-8SnHIXLLjVqA__imagepage/img/kIBu-2Ljb_ml5n-S8uIbE6ehGFc=/fit-in/900x600/filters:no_upscale():strip_icc()/pic1534148.jpg",2,4,
                                List.of("Cooperative", "Strategy")),
                        new Boardgame(null,9209,"Ticket to Ride","Railway route-building game.",
                                "https://cf.geekdo-images.com/kdWYkW-7AqG63HhqPL6ekA__imagepage/img/AWsdGNNSuI78BaCPAVQpjrUneKY=/fit-in/900x600/filters:no_upscale():strip_icc()/pic8937637.jpg",2,5,
                                List.of("Strategy","Trains","Transportation")),
                        new Boardgame(null, 171, "Chess", "Classic two-player strategy game played on an 8x8 board.", "https://new.uschess.org/sites/default/files/styles/1080px_wide_scale/public/media/images/2024_cover_image.png.webp?itok=xUbyXJ_i", 2,2,
                                List.of("Abstract Strategy", "Classic"))
                );
                boardGameRepository.saveAll(boardGames);
                System.out.println("Seeded " + boardGames.size() + " board games");
            } else {
                System.out.println("Board games already seeded, skipping...");
            }

            // Groups
            if (groupRepository.count() == 0) {
                List<String> usernames = List.of("IAmR3al", "sarah_dev", "bob", "alex_games", "jane_doe");
                List<Group> groups = List.of(
                    new Group("Board Game Enthusiasts",
                    null, 
                    "A group for all board game lovers.", 
                    "General",
                    null , 
                    "public"),
                    new Group("Strategy Masters",
                    null, 
                    "Deep strategy games discussion.", 
                    "Strategy",
                    null, 
                    "public"),
                    new Group("Casual Gamers",
                    null, 
                    "Laid back gaming sessions and trades.", 
                    "General",
                    null, 
                    "public"),
                    new Group("RPG Adventurers",
                    null, 
                    "Tabletop RPG and dungeon crawler fans.",
                    "Role-Playing", 
                    null , 
                    "private"),
                    new Group("Card & Tile Collectors",
                    null, 
                    "For fans of card and tile-based games.", 
                    "General",
                    null, 
                    "private")
                );
                
                for(int i = 0; i < 5; i++){
                    User user = userRepository.findByUsername(usernames.get(i)).get();
                    groups.get(i).setOwnerId(user.getId());
                }


                groupRepository.saveAll(groups);
                System.out.println("Seeded " + groups.size() + " groups");
            } else {
                System.out.println("Groups already seeded, skipping...");
            }
            // Group Memberships
            if (groupMembershipRepository.count() == 0) {
                List<GroupMembership> memberships = groupRepository.findAll().stream()
                        .flatMap(group -> {
                            List<String> members = switch (group.getName()) {
                                case "Board Game Enthusiasts" -> List.of("IAmR3al", "bob", "jane_doe", "mike_b");
                                case "Strategy Masters" -> List.of("sarah_dev", "alex_games", "IAmR3al");
                                case "Casual Gamers" -> List.of("bob", "mike_b", "jane_doe");
                                case "RPG Adventurers" -> List.of("alex_games", "sarah_dev", "IAmR3al");
                                case "Card & Tile Collectors" -> List.of("jane_doe", "bob", "IAmR3al");
                                default -> List.of();
                            };


                            return members.stream().map((username) ->{
                                User user = userRepository.findByUsername(username).get();
                                return new GroupMembership(user.getId(), group.getId());
                            });
                        })
                        .toList();
                groupMembershipRepository.saveAll(memberships);
                System.out.println("Seeded " + memberships.size() + " group memberships");
            } else {
                System.out.println("Group memberships already seeded, skipping...");
            }
            // Rulebooks
            if (rulebookRepository.count() == 0) {
                record Contributor(ObjectId id, String username){}
                Contributor con1 = new Contributor(new ObjectId(), "JustUploadsStuff");
                Contributor con2 = new Contributor(new ObjectId(), "MiteBeReliable");

                Map<String, ObjectId> gameIdsByTitle = boardGameRepository.findAll().stream()
                        .collect(Collectors.toMap(Boardgame::getTitle, bg -> new ObjectId(bg.getId())));

                List<Rulebook> rulebooks = List.of(
                        Rulebook.builder().coverUrl("https://pub-c543dd80255b4b9c9c31a54e09389b5d.r2.dev/listings/Monopoly/Monopoly.png").gameId(gameIdsByTitle.get("Monopoly")).title("Monopoly").edition("Classic").status("Ready").version(1)
                                .contributorId(con1.id()).contributorUsername(con1.username()).description("Objective: Bankrupt all opposing players by acquiring, developing, and trading real estate properties.").language("English").r2PdfKey("rulebooks/monopoly-classic.pdf")
                                .r2CoverKey("/rulebooks/default_cover.png").uploadedAt(Instant.now()).updatedAt(Instant.now()).build(),
                        Rulebook.builder().coverUrl("https://pub-c543dd80255b4b9c9c31a54e09389b5d.r2.dev/listings/Scrabble/Scrabble.jpg").gameId(gameIdsByTitle.get("Scrabble")).title("Scrabble").edition("Standard").status("Ready").version(1)
                                .contributorId(con1.id()).contributorUsername(con1.username()).description("Objective: Accumulate the highest score by spelling interlocking, valid dictionary words on a grid.").language("English").r2PdfKey("rulebooks/scrabble-standard.pdf")
                                .r2CoverKey("/rulebooks/default_cover.png").uploadedAt(Instant.now()).updatedAt(Instant.now()).build(),
                        Rulebook.builder().coverUrl("").gameId(gameIdsByTitle.get("Catan")).title("Catan").edition("5th Edition").status("Ready").version(2)
                                .contributorId(con2.id()).contributorUsername(con2.username()).description("Objective: Be the first player to accumulate 10 Victory Points (VPs).").language("Spanish").r2PdfKey("rulebooks/catan-5th.pdf")
                                .r2CoverKey("/rulebooks/default_cover.png").uploadedAt(Instant.now()).updatedAt(Instant.now()).build(),
                        Rulebook.builder().coverUrl("https://cf.geekdo-images.com/S3ybV1LAp-8SnHIXLLjVqA__imagepage/img/kIBu-2Ljb_ml5n-S8uIbE6ehGFc=/fit-in/900x600/filters:no_upscale():strip_icc()/pic1534148.jpg").gameId(gameIdsByTitle.get("Pandemic")).title("Pandemic").edition("2nd Edition").status("PendingReview")
                                .version(1).contributorId(con2.id()).contributorUsername(con2.username()).description("Objective: Work cooperatively to discover cures for four distinct global diseases before a failure condition is triggered.").language("Spanish").r2PdfKey("rulebooks/pandemic-2nd.pdf")
                                .r2CoverKey("/rulebooks/default_cover.png").uploadedAt(Instant.now()).updatedAt(Instant.now()).build(),
                        Rulebook.builder().coverUrl("https://cf.geekdo-images.com/kdWYkW-7AqG63HhqPL6ekA__imagepage/img/AWsdGNNSuI78BaCPAVQpjrUneKY=/fit-in/900x600/filters:no_upscale():strip_icc()/pic8937637.jpg").gameId(gameIdsByTitle.get("Ticket to Ride")).title("Ticket to Ride").edition("Original").status("Processing")
                                .version(1).contributorId(con1.id()).contributorUsername(con1.username()).description("Objective: Score the highest number of points by claiming railway routes and completing hidden Destination Tickets.").language("French").r2PdfKey("rulebooks/ticket-to-ride.pdf")
                                .r2CoverKey("/rulebooks/default_cover.png").uploadedAt(Instant.now()).updatedAt(Instant.now()).build());
                rulebookRepository.saveAll(rulebooks);
                System.out.println("Seeded " + rulebooks.size() + " rulebooks");

                // Rulebook Texts
                List<Chunk> monopolyChunks = List.of(
                        Chunk.builder().chunkId(new ObjectId()).index(0).content("Objective: Bankrupt all opposing players by acquiring, developing, and trading real estate properties.").build(),
                        Chunk.builder().chunkId(new ObjectId()).index(1).content("Turn Structure:\n" + //
                                                                "\n" + //
                                                                "Roll two six-sided dice and move your token clockwise.\n" + //
                                                                "\n" + //
                                                                "Resolve the effect of the landed space (purchase unowned property, pay rent to the owner, draw a Chance/Community Chest card, pay taxes, or go to Jail).\n" + //
                                                                "\n" + //
                                                                "Rolling doubles grants an additional turn; rolling three consecutive doubles sends you immediately to Jail.").build(),
                        Chunk.builder().chunkId(new ObjectId()).index(2).content("Key Mechanics: Owning a complete color set allows for the construction of houses and hotels, significantly increasing rent. Properties can be mortgaged to the bank for emergency liquidity.").build(),
                        Chunk.builder().chunkId(new ObjectId()).index(3).content("End Game: The game concludes when only one player remains solvent.").build()
                );
                List<Chunk> scrabbleChunks = List.of(
                        Chunk.builder().chunkId(new ObjectId()).index(0).content("Objective: Accumulate the highest score by spelling interlocking, valid dictionary words on a grid.").build(),
                        Chunk.builder().chunkId(new ObjectId()).index(1).content("Turn Structure:\n" + //
                                                                "\n" + //
                                                                "Choose one action: Place tiles to form/extend a word, exchange any number of tiles, or pass.\n" + //
                                                                "\n" + //
                                                                "Calculate the score of the newly formed word(s) using individual letter values and board multipliers (Double/Triple Letter or Word squares).\n" + //
                                                                "\n" + //
                                                                "Draw replacement tiles from the bag to restore your rack to exactly seven tiles.").build(),
                        Chunk.builder().chunkId(new ObjectId()).index(2).content("Key Mechanics: Playing all seven tiles in a single turn awards a 50-point bonus (a \"Bingo\").").build(),
                        Chunk.builder().chunkId(new ObjectId()).index(3).content("End Game: The game ends when the tile bag is empty and one player clears their rack, or when no further valid plays are possible.").build()
                );
                List<Chunk> catanChunks = List.of(
                        Chunk.builder().chunkId(new ObjectId()).index(0).content("Objective: Be the first player to accumulate 10 Victory Points (VPs).").build(),
                        Chunk.builder().chunkId(new ObjectId()).index(1).content("Turn Structure:\n" + //
                                                                "\n" + //
                                                                "Roll two dice to determine resource production for the turn. All players with settlements or cities adjacent to the rolled hex number collect corresponding resource cards.\n" + //
                                                                "\n" + //
                                                                "The active player may trade resources with other players or the bank (at a 4:1 baseline ratio).\n" + //
                                                                "\n" + //
                                                                "The active player may spend resources to build roads, settlements, cities, or buy Development Cards.").build(),
                        Chunk.builder().chunkId(new ObjectId()).index(2).content("Key Mechanics: Rolling a 7 activates the Robber: no resources are produced, players with more than 7 cards must discard half, and the active player moves the Robber to block a hex and steal one resource from an adjacent player.").build(),
                        Chunk.builder().chunkId(new ObjectId()).index(3).content("End Game: The game ends immediately on an active player's turn the moment they have 10 or more VPs.").build()
                );
                List<Chunk> pandemicChunks = List.of(
                        Chunk.builder().chunkId(new ObjectId()).index(0).content("Objective: Work cooperatively to discover cures for four distinct global diseases before a failure condition is triggered.").build(),
                        Chunk.builder().chunkId(new ObjectId()).index(1).content("Turn Structure:\n" + //
                                                                "\n" + //
                                                                "Take up to four actions (move around the map, treat disease cubes, share knowledge cards with another player, build a research station, or discard five matching city cards to discover a cure).\n" + //
                                                                "\n" + //
                                                                "Draw two Player cards. If an Epidemic card is drawn, the infection rate increases, the bottom card of the infection deck is drawn (adding 3 disease cubes), and the infection discard pile is shuffled and placed back on top of the deck.\n" + //
                                                                "\n" + //
                                                                "Draw Infection cards equal to the current infection rate, adding one disease cube to each drawn city.").build(),
                        Chunk.builder().chunkId(new ObjectId()).index(2).content("Key Mechanics: If a city requires a fourth disease cube of the same color, an Outbreak occurs instead, spreading cubes to all connected cities and increasing the Outbreak tracker.").build(),
                        Chunk.builder().chunkId(new ObjectId()).index(3).content("End Game: Players win immediately if all four cures are discovered. Players lose if the Outbreak tracker reaches eight, if any disease cube color runs out, or if a player needs to draw from an empty Player deck.").build()
                );
                List<Chunk> ticketToRideChunks = List.of(
                        Chunk.builder().chunkId(new ObjectId()).index(0).content("Objective: Score the highest number of points by claiming railway routes and completing hidden Destination Tickets.").build(),
                        Chunk.builder().chunkId(new ObjectId()).index(1).content("Turn Structure:\n" + //
                                                                "\n" + //
                                                                "Choose exactly one action per turn:\n" + //
                                                                "\n" + //
                                                                "Draw two Train Car cards (from the face-up pool or blind from the deck).\n" + //
                                                                "\n" + //
                                                                "Claim a route by discarding a set of matching colored cards equal to the route's length and placing plastic trains on the board (scoring points immediately).\n" + //
                                                                "\n" + //
                                                                "Draw three Destination Tickets and keep at least one.").build(),
                        Chunk.builder().chunkId(new ObjectId()).index(2).content("Key Mechanics: Destination Tickets provide bonus points at the end of the game if the listed cities are successfully connected, but subtract their point value if incomplete.").build(),
                        Chunk.builder().chunkId(new ObjectId()).index(3).content("End Game: The final round triggers when any player's plastic train supply drops to two or fewer. At the end of the game, a 10-point bonus is awarded to the player with the single longest continuous path.").build()
                );
                List<RulebookText> texts = List.of(
                        RulebookText.builder().rulebookId(rulebooks.get(0).getId()).version(0).chunks(monopolyChunks).updatedAt(Instant.now().plusSeconds(0)).build(),
                        RulebookText.builder().rulebookId(rulebooks.get(1).getId()).version(1).chunks(scrabbleChunks).updatedAt(Instant.now().plusSeconds(10)).build(),
                        RulebookText.builder().rulebookId(rulebooks.get(2).getId()).version(2).chunks(catanChunks).updatedAt(Instant.now().plusSeconds(50)).build(),
                        RulebookText.builder().rulebookId(rulebooks.get(3).getId()).version(3).chunks(pandemicChunks).updatedAt(Instant.now().plusSeconds(100)).build(),
                        RulebookText.builder().rulebookId(rulebooks.get(4).getId()).version(4).chunks(ticketToRideChunks).updatedAt(Instant.now().plusSeconds(500)).build());
                rulebookTextRepository.saveAll(texts);
                System.out.println("Seeded " + texts.size() + " rulebook texts");

                // Ingestion Jobs
                List<IngestionJob> jobs = List.of(
                        IngestionJob.builder().rulebookId(rulebooks.get(0).getId()).stage("Extract").jobStatus("Ready")
                                .startedAt(Instant.now().minusSeconds(300)).completedAt(Instant.now()).build(),
                        IngestionJob.builder().rulebookId(rulebooks.get(1).getId()).stage("Extract").jobStatus("Ready")
                                .startedAt(Instant.now().minusSeconds(200)).completedAt(Instant.now()).build(),
                        IngestionJob.builder().rulebookId(rulebooks.get(2).getId()).stage("Sanitise").jobStatus("Ready")
                                .startedAt(Instant.now().minusSeconds(500)).completedAt(Instant.now()).build(),
                        IngestionJob.builder().rulebookId(rulebooks.get(3).getId()).stage("Extract")
                                .jobStatus("PendingReview").startedAt(Instant.now().minusSeconds(100)).completedAt(null)
                                .build(),
                        IngestionJob.builder().rulebookId(rulebooks.get(4).getId()).stage("Sanitise")
                                .jobStatus("Processing").startedAt(Instant.now().minusSeconds(60)).completedAt(null)
                                .build());
                ingestionJobRepository.saveAll(jobs);
                System.out.println("Seeded " + jobs.size() + " ingestion jobs");

                // Edit Events
                List<EditEvent> editEvents = List.of(
                        EditEvent.builder().rulebookId(rulebooks.get(0).getId()).editorId(con1.id())
                                .delta("Fixed typo on page 3.").versionAfter(2)
                                .committedAt(Instant.now().minusSeconds(120)).build(),
                        EditEvent.builder().rulebookId(rulebooks.get(1).getId()).editorId(con1.id())
                                .delta("Updated scoring section.").versionAfter(2)
                                .committedAt(Instant.now().minusSeconds(90)).build(),
                        EditEvent.builder().rulebookId(rulebooks.get(2).getId()).editorId(con2.id())
                                .delta("Clarified trading rules.").versionAfter(3)
                                .committedAt(Instant.now().minusSeconds(60)).build());
                editEventRepository.saveAll(editEvents);
                System.out.println("Seeded " + editEvents.size() + " edit events");

                // Write Locks
            } else {
                System.out.println("Rulebooks already seeded, skipping...");
            }
        };
    }
}
