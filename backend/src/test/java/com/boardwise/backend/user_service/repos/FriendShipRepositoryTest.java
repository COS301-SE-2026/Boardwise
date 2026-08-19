package com.boardwise.backend.user_service.repos;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import com.boardwise.backend.SharedMongoContainer;
import com.boardwise.backend.user_service.enums.FriendStatus;
import com.boardwise.backend.user_service.fixtures.ProfileServiceFixtures;
import com.boardwise.backend.user_service.models.Friendship;

@DataMongoTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("FriendShip Repository Tests")
public class FriendShipRepositoryTest extends SharedMongoContainer{

    @Autowired
    private FriendShipRepository fsRepo;
    
    @BeforeEach
    void setUp(){
        System.out.println("Set up database (Friendship collection)");
        List<Friendship> friendships = List.of(
            ProfileServiceFixtures.friendship1(),
            ProfileServiceFixtures.friendship2(),
            ProfileServiceFixtures.friendship3(),
            ProfileServiceFixtures.friendship4(),
            ProfileServiceFixtures.friendship5()
        );

        fsRepo.saveAll(friendships);
    }

    @ParameterizedTest
    @MethodSource("statusProvider")
    void shouldTestCorrectnessOfFindByUserAndStatus(FriendStatus status){
        // Arrange
        String userId = ProfileServiceFixtures.FRIEND_ID1;

        // Act
        List<Friendship> results = fsRepo.findByUserAndStatus(userId, status);

        // Assert
        if(status == FriendStatus.ACCEPTED){
            assertEquals(2, results.size());

            assertEquals("fs-001", results.get(0).getId());
            assertEquals(FriendStatus.ACCEPTED, results.get(0).getStatus());
            assertEquals(userId, results.get(0).getReceiver());
            assertEquals(ProfileServiceFixtures.OWNER_ID, results.get(0).getSender());

            assertEquals("fs-003", results.get(1).getId());
            assertEquals(FriendStatus.ACCEPTED, results.get(1).getStatus());
            assertEquals(userId, results.get(1).getReceiver());
            assertEquals(ProfileServiceFixtures.FRIEND_ID3, results.get(1).getSender());
        }
        else{
            assertEquals(1, results.size());
            assertEquals("fs-004", results.get(0).getId());
            assertEquals(FriendStatus.REQUESTED, results.get(0).getStatus());
            assertEquals(userId, results.get(0).getSender());
            assertEquals(ProfileServiceFixtures.FRIEND_ID2, results.get(0).getReceiver());
        }
    }

    @Test
    void shouldTestCorrectnessOfFindFriendShipBetweenUsers(){
        // Arrange
        String userAId = ProfileServiceFixtures.FRIEND_ID1,
                userBId = ProfileServiceFixtures.OWNER_ID;
        
        // Act
        Optional<Friendship> result = fsRepo.findFriendShipBetweenUsers(userAId, userBId);

        // Assert
        assertThat(result.isPresent()).isTrue();

        Friendship fs = result.get();
        assertEquals(userBId, fs.getSender());
        assertEquals(userAId, fs.getReceiver());
        assertEquals(FriendStatus.ACCEPTED, fs.getStatus());
    }

    Stream<Arguments> statusProvider(){
        return Stream.of(
            Arguments.of(FriendStatus.ACCEPTED),
            Arguments.of(FriendStatus.REQUESTED)
        );
    }
    
}
