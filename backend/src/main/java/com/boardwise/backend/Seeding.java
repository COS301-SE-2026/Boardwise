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
import com.boardwise.backend.vault.model.WriteLock;
import com.boardwise.backend.vault.repository.EditEventRepository;
import com.boardwise.backend.vault.repository.IngestionJobRepository;
import com.boardwise.backend.vault.repository.RulebookRepository;
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
            IngestionJobRepository ingestionJobRepository, RulebookRepository rulebookRepository,
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
                                "https://pub-c543dd80255b4b9c9c31a54e09389b5d.r2.dev/listings/Monopoly/Monopoly.png", List.of("Strategy", "Trading")),
                        new Boardgame(null, 2,"Scrabble", "Word building board game.",
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
                    new Group("Board Game Enthusiasts", 
                    "A group for all board game lovers.", 
                    "General",
                    null , 
                    "public"),
                    new Group("Strategy Masters", 
                    "Deep strategy games discussion.", 
                    "Strategy",
                    null, 
                    "public"),
                    new Group("Casual Gamers", 
                    "Laid back gaming sessions and trades.", 
                    "General",
                    null, 
                    "public"),
                    new Group("RPG Adventurers", 
                    "Tabletop RPG and dungeon crawler fans.",
                    "Role-Playing", 
                    null , 
                    "private"),
                    new Group("Card & Tile Collectors", 
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
