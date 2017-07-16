package contactBook.service;

import contactBook.model.User;
import contactBook.repository.UserRepository;
import contactBook.service.Encryptor.PasswordEncryptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service("provider")
public class CustomAuthenticationProvider implements AuthenticationProvider {
    private final UserRepository userRepository;
    private PasswordEncryptor encryptor;

    @Autowired
    public CustomAuthenticationProvider(UserRepository userRepository, PasswordEncryptor encryptor) {
        this.userRepository = userRepository;
        this.encryptor = encryptor;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        User user = userRepository.getUserByUserName(authentication.getName());
        if(user==null) return null;
        if(encryptor.encryptPassword(authentication.getCredentials().toString(),authentication.getName()).equals(user.getPassword())){
            ArrayList<GrantedAuthority> roles = new ArrayList<>();
            roles.add(new SimpleGrantedAuthority("ROLE_USER"));
            return new UsernamePasswordAuthenticationToken(authentication.getName(),authentication.getCredentials(),roles);
        }else return null;
    }

    @Override
    public boolean supports(Class<?> aClass) {
        return UsernamePasswordAuthenticationToken.class.equals(aClass);
    }
}
