package de.kaliburg.morefair;

import lombok.Getter;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserService
{
    @Getter
    private List<User> users;

    public UserService()
    {
        users = new ArrayList<User>();
        users.add(new User("Kaliburg", 1, 100));
        users.add(new User("tobi", 2, 50));
        users.add(new User("mystery user 7", 2, 50));
    }

    User saveUser(User user){
        users.add(user);

        return user;
    }
}
