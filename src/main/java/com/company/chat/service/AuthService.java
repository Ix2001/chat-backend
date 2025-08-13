import com.company.chat.repository.UserRepository;
import com.company.chat.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepo;

    public String login(String username, String rawPassword) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, rawPassword)
        );

        var user = userRepo.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(username));

        return jwtService.generate(user.getUsername(), user.getRoles()); // твой способ генерации
    }
}
