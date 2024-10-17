package com.example.relicv2.security;

import com.example.relicv2.exception.BiddingUnauthorizedException;
import com.example.relicv2.exception.CustomException;
import com.example.relicv2.exception.ResourceNotFoundException;
import com.example.relicv2.model.User;
import com.example.relicv2.repository.UserRepository;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenProvider {
    @Value("${app.jwt-secret}")
    private String jwtSecret;

    @Value("${app.jwt-access-token-expiration}")
    private long jwtExpirationDate;

    @Value("${app.jwt-verification-token-expiration}")
    private long jwtVerificationTokenExpiration;

    @Autowired
    private UserRepository userRepository;

    public String generateAccessToken(Authentication authentication) {
        String username = authentication.getName();
        Date currentDate = new Date();
        Date expireDate = new Date(currentDate.getTime() + jwtExpirationDate);
        String token = Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(expireDate)
                .signWith(key())
                .compact();
        return token;
    }

    private Key key(){
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    // get username from JWT token
    public String getUsername(String token){

        return Jwts.parser()
                .verifyWith((SecretKey) key())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public String generateVerificationToken(User user) {
        Date currentDate = new Date();
        Date expireDate = new Date(currentDate.getTime() + jwtVerificationTokenExpiration);

        return Jwts.builder()
                .setSubject(user.getUsername())
                .setIssuedAt(currentDate)
                .setExpiration(expireDate)
                .signWith(key())
                .compact();
    }

    public User getUserFromToken(String token) {
        if (validateToken(token)) {
            String username = getUsername(token);
            return userRepository.findByUsername(username)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        }
        throw new BiddingUnauthorizedException("Invalid token");
    }

    // validate JWT token
    public boolean validateToken(String token){
        try{
            Jwts.parser()
                    .verifyWith((SecretKey) key())
                    .build()
                    .parse(token);
            return true;
        }catch (MalformedJwtException malformedJwtException){
            throw new CustomException("Invalid JWT Token");
        }catch (ExpiredJwtException expiredJwtException){
            throw new CustomException("Expired JWT token");
        }catch (UnsupportedJwtException unsupportedJwtException){
            throw new CustomException("Unsupported JWT token");
        }catch (IllegalArgumentException illegalArgumentException){
            throw new CustomException("Jwt claims string is null or empty");
        }
    }

    public long getJwtExpirationDate() {
        return jwtExpirationDate;
    }
}
