package com.boardwise.backend;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;


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
import com.boardwise.backend.vault.model.EditEvent;
import com.boardwise.backend.vault.model.IngestionJob;
import com.boardwise.backend.vault.model.Rulebook;
import com.boardwise.backend.vault.model.RulebookText;
import com.boardwise.backend.vault.model.WriteLock;
import com.boardwise.backend.vault.repository.EditEventRepository;
import com.boardwise.backend.vault.repository.IngestionJobRepository;
import com.boardwise.backend.vault.repository.RulebookRepository;
import com.boardwise.backend.vault.repository.RulebookTextRepository;
import com.boardwise.backend.vault.repository.WriteLockRepository;

@Component
@Profile("!test")
public class Seeding {
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
                        new User("mike_b", "Michael", "Brown", "michael.brown@outlook.com", encoder.encode("Br0wn!Mike_7")));
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

                List<Listing> listings = List.of(
                        new Listing(null, "IAmR3al", "boardgame", "sale", 29.99, "Monopoly",
                                "Monopoly game with all details\n", "https://pub-c543dd80255b4b9c9c31a54e09389b5d.r2.dev/listings/Monopoly/Monopoly.png",
                                ListingStatus.AVAILABLE, LocalDateTime.now(), LocalDateTime.now(),
                                List.of("Strategy", "Action"), null),
                        new Listing(null, "sarah_dev", "boardgame", "rental", 48.32, "Scrabble",
                                "game of scrabble with Missing pieces", "https://pub-c543dd80255b4b9c9c31a54e09389b5d.r2.dev/listings/Scrabble/Scrabble.jpg",
                                ListingStatus.AVAILABLE, LocalDateTime.now().plusDays(5),
                                LocalDateTime.now().plusDays(5), List.of("abstract strategy"), rentalPeriod1));

                listingRepository.saveAll(listings);
                System.out.println("Seeded " + listings.size() + " listings");
            } else {
                System.out.println("Listings already seeded, skipping...");
            }

            if (boardGameRepository.count() == 0) {
                List<Boardgame> boardGames = List.of(
                        new Boardgame(null, "Monopoly", "Classic property trading game.",
                                "https://pub-c543dd80255b4b9c9c31a54e09389b5d.r2.dev/listings/Monopoly/Monopoly.png", List.of("Strategy", "Trading")),
                        new Boardgame(null, "Scrabble", "Word building board game.",
                                "https://pub-c543dd80255b4b9c9c31a54e09389b5d.r2.dev/listings/Scrabble/Scrabble.jpg", List.of("Word", "Abstract Strategy")));
                boardGameRepository.saveAll(boardGames);
                System.out.println("Seeded " + boardGames.size() + " board games");
            } else {
                System.out.println("Board games already seeded, skipping...");
            }

            // Groups
            if (groupRepository.count() == 0) {
                List<String> usernames = List.of("IAmR3al", "sarah_dev", "bob", "alex_games", "jane_doe");
                List<Group> groups = List.of(
                    new Group("Board Game Enthusiasts", "A group for all board game lovers.", null , "public"),
                    new Group("Strategy Masters", "Deep strategy games discussion.", null , "public"),
                    new Group("Casual Gamers", "Laid back gaming sessions and trades.", null , "public"),
                    new Group("RPG Adventurers", "Tabletop RPG and dungeon crawler fans.", null , "private"),
                    new Group("Card & Tile Collectors", "For fans of card and tile-based games.", null,
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
                ObjectId contributor1 = new ObjectId();
                ObjectId contributor2 = new ObjectId();

                List<Rulebook> rulebooks = List.of(
                        Rulebook.builder().gameName("Monopoly").edition("Classic").status("Ready").version(1)
                                .contributorId(contributor1).r2PdfKey("rulebooks/monopoly-classic.pdf")
                                .uploadedAt(Instant.now()).updatedAt(Instant.now()).build(),
                        Rulebook.builder().gameName("Scrabble").edition("Standard").status("Ready").version(1)
                                .contributorId(contributor1).r2PdfKey("rulebooks/scrabble-standard.pdf")
                                .uploadedAt(Instant.now()).updatedAt(Instant.now()).build(),
                        Rulebook.builder().gameName("Catan").edition("5th Edition").status("Ready").version(2)
                                .contributorId(contributor2).r2PdfKey("rulebooks/catan-5th.pdf")
                                .uploadedAt(Instant.now()).updatedAt(Instant.now()).build(),
                        Rulebook.builder().gameName("Pandemic").edition("2nd Edition").status("PendingReview")
                                .version(1).contributorId(contributor2).r2PdfKey("rulebooks/pandemic-2nd.pdf")
                                .uploadedAt(Instant.now()).updatedAt(Instant.now()).build(),
                        Rulebook.builder().gameName("Ticket to Ride").edition("Original").status("Processing")
                                .version(1).contributorId(contributor1).r2PdfKey("rulebooks/ticket-to-ride.pdf")
                                .uploadedAt(Instant.now()).updatedAt(Instant.now()).build());
                rulebookRepository.saveAll(rulebooks);
                System.out.println("Seeded " + rulebooks.size() + " rulebooks");

                // Rulebook Text
                List<RulebookText> rulebookTexts = List.of(
                        RulebookText.builder().rulebookId(rulebooks.get(0).getId()).content("Objective: Bankrupt all opposing players by acquiring, developing, and trading real estate properties.\n" + //
                                                                "\n" + //
                                                                "Turn Structure:\n" + //
                                                                "\n" + //
                                                                "Roll two six-sided dice and move your token clockwise.\n" + //
                                                                "\n" + //
                                                                "Resolve the effect of the landed space (purchase unowned property, pay rent to the owner, draw a Chance/Community Chest card, pay taxes, or go to Jail).\n" + //
                                                                "\n" + //
                                                                "Rolling doubles grants an additional turn; rolling three consecutive doubles sends you immediately to Jail.\n" + //
                                                                "\n" + //
                                                                "Key Mechanics: Owning a complete color set allows for the construction of houses and hotels, significantly increasing rent. Properties can be mortgaged to the bank for emergency liquidity.\n" + //
                                                                "\n" + //
                                                                "End Game: The game concludes when only one player remains solvent.").version(rulebooks.get(0).getVersion()).updatedAt(rulebooks.get(0).getUpdatedAt()).build(),
                        RulebookText.builder().rulebookId(rulebooks.get(1).getId()).content("Objective: Accumulate the highest score by spelling interlocking, valid dictionary words on a grid.\n" + //
                                                                "\n" + //
                                                                "Turn Structure:\n" + //
                                                                "\n" + //
                                                                "Choose one action: Place tiles to form/extend a word, exchange any number of tiles, or pass.\n" + //
                                                                "\n" + //
                                                                "Calculate the score of the newly formed word(s) using individual letter values and board multipliers (Double/Triple Letter or Word squares).\n" + //
                                                                "\n" + //
                                                                "Draw replacement tiles from the bag to restore your rack to exactly seven tiles.\n" + //
                                                                "\n" + //
                                                                "Key Mechanics: Playing all seven tiles in a single turn awards a 50-point bonus (a \"Bingo\").\n" + //
                                                                "\n" + //
                                                                "End Game: The game ends when the tile bag is empty and one player clears their rack, or when no further valid plays are possible.").version(rulebooks.get(1).getVersion()).updatedAt(rulebooks.get(1).getUpdatedAt()).build(),
                        RulebookText.builder().rulebookId(rulebooks.get(2).getId()).content("Objective: Be the first player to accumulate 10 Victory Points (VPs).\n" + //
                                                                "\n" + //
                                                                "Turn Structure:\n" + //
                                                                "\n" + //
                                                                "Roll two dice to determine resource production for the turn. All players with settlements or cities adjacent to the rolled hex number collect corresponding resource cards.\n" + //
                                                                "\n" + //
                                                                "The active player may trade resources with other players or the bank (at a 4:1 baseline ratio).\n" + //
                                                                "\n" + //
                                                                "The active player may spend resources to build roads, settlements, cities, or buy Development Cards.\n" + //
                                                                "\n" + //
                                                                "Key Mechanics: Rolling a 7 activates the Robber: no resources are produced, players with more than 7 cards must discard half, and the active player moves the Robber to block a hex and steal one resource from an adjacent player.\n" + //
                                                                "\n" + //
                                                                "End Game: The game ends immediately on an active player's turn the moment they have 10 or more VPs.").version(rulebooks.get(2).getVersion()).updatedAt(rulebooks.get(2).getUpdatedAt()).build(),
                        RulebookText.builder().rulebookId(rulebooks.get(3).getId()).content("Objective: Work cooperatively to discover cures for four distinct global diseases before a failure condition is triggered.\n" + //
                                                                "\n" + //
                                                                "Turn Structure:\n" + //
                                                                "\n" + //
                                                                "Take up to four actions (move around the map, treat disease cubes, share knowledge cards with another player, build a research station, or discard five matching city cards to discover a cure).\n" + //
                                                                "\n" + //
                                                                "Draw two Player cards. If an Epidemic card is drawn, the infection rate increases, the bottom card of the infection deck is drawn (adding 3 disease cubes), and the infection discard pile is shuffled and placed back on top of the deck.\n" + //
                                                                "\n" + //
                                                                "Draw Infection cards equal to the current infection rate, adding one disease cube to each drawn city.\n" + //
                                                                "\n" + //
                                                                "Key Mechanics: If a city requires a fourth disease cube of the same color, an Outbreak occurs instead, spreading cubes to all connected cities and increasing the Outbreak tracker.\n" + //
                                                                "\n" + //
                                                                "End Game: Players win immediately if all four cures are discovered. Players lose if the Outbreak tracker reaches eight, if any disease cube color runs out, or if a player needs to draw from an empty Player deck.").version(rulebooks.get(3).getVersion()).updatedAt(rulebooks.get(3).getUpdatedAt()).build(),
                        RulebookText.builder().rulebookId(rulebooks.get(4).getId()).content("Objective: Score the highest number of points by claiming railway routes and completing hidden Destination Tickets.\n" + //
                                                                "\n" + //
                                                                "Turn Structure:\n" + //
                                                                "\n" + //
                                                                "Choose exactly one action per turn:\n" + //
                                                                "\n" + //
                                                                "Draw two Train Car cards (from the face-up pool or blind from the deck).\n" + //
                                                                "\n" + //
                                                                "Claim a route by discarding a set of matching colored cards equal to the route's length and placing plastic trains on the board (scoring points immediately).\n" + //
                                                                "\n" + //
                                                                "Draw three Destination Tickets and keep at least one.\n" + //
                                                                "\n" + //
                                                                "Key Mechanics: Destination Tickets provide bonus points at the end of the game if the listed cities are successfully connected, but subtract their point value if incomplete.\n" + //
                                                                "\n" + //
                                                                "End Game: The final round triggers when any player's plastic train supply drops to two or fewer. At the end of the game, a 10-point bonus is awarded to the player with the single longest continuous path.").version(rulebooks.get(4).getVersion()).updatedAt(rulebooks.get(4).getUpdatedAt()).build());
                rulebookTextRepository.saveAll(rulebookTexts);
                System.out.println("Seeded " + rulebookTexts.size() + " rulebookTexts");

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
                        EditEvent.builder().rulebookId(rulebooks.get(0).getId()).editorId(contributor1)
                                .delta("Fixed typo on page 3.").versionAfter(2)
                                .committedAt(Instant.now().minusSeconds(120)).build(),
                        EditEvent.builder().rulebookId(rulebooks.get(1).getId()).editorId(contributor1)
                                .delta("Updated scoring section.").versionAfter(2)
                                .committedAt(Instant.now().minusSeconds(90)).build(),
                        EditEvent.builder().rulebookId(rulebooks.get(2).getId()).editorId(contributor2)
                                .delta("Clarified trading rules.").versionAfter(3)
                                .committedAt(Instant.now().minusSeconds(60)).build());
                editEventRepository.saveAll(editEvents);
                System.out.println("Seeded " + editEvents.size() + " edit events");

                // Write Locks
                List<WriteLock> writeLocks = List.of(
                        WriteLock.builder().rulebookId(rulebooks.get(3).getId()).heldByUserId(contributor2)
                                .acquiredAt(Instant.now().minusSeconds(10)).expiresAt(Instant.now().plusSeconds(20))
                                .build());
                writeLockRepository.saveAll(writeLocks);
                System.out.println("Seeded " + writeLocks.size() + " write locks");

            } else {
                System.out.println("Rulebooks already seeded, skipping...");
            }
        };
    }
}
