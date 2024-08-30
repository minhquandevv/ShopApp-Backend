package com.project.shopapp.components;

import com.project.shopapp.models.User;
import com.project.shopapp.exceptions.InvalidParamException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.*;
import java.util.function.Function;

@Component
@RequiredArgsConstructor
public class JwtTokenUtils {
  @Value("${jwt.expiration}")
  private Long expiration; //Save to ENV

  @Value("${jwt.secretKey}")
  private String secretKey;

  public String generateToken(User user) throws Exception {
    Map<String, Object> claims = new HashMap<>();
//    this.generateSecretKey();
    claims.put("phoneNumber", user.getPhoneNumber());
    try {
      return Jwts.builder()
          .setClaims(claims) //how to extract claims from this ?
          .setSubject(user.getPhoneNumber())
          .setExpiration(new Date(System.currentTimeMillis() + expiration * 1000L))
          .signWith(getSignInKey(), SignatureAlgorithm.HS256)
          .compact();
    } catch (Exception e) {
      //you can "inject" Logger, instead System.out.println
      throw new InvalidParamException("Cannot create jwt token, error: " + e.getMessage());
      //return null;
    }
  }

  private Key getSignInKey() {
    byte[] bytes = Decoders.BASE64.decode(secretKey);
    //Keys.hmacShaKeyFor(Decoders.BASE64.decode("Key Token"));
    return Keys.hmacShaKeyFor(bytes);
  }

//  private String generateSecretKey() {
//    SecureRandom random = new SecureRandom();
//    byte[] bytes = new byte[32];
//    random.nextBytes(bytes);
//    String secretKey = Encoders.BASE64.encode(bytes);
//    return secretKey;
//  }

  private Claims extractAllClaims(String token) {
    return Jwts.parserBuilder()
        .setSigningKey(getSignInKey())
        .build()
        .parseClaimsJws(token)
        .getBody();
  }

  public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
    final Claims claims = this.extractAllClaims(token);
    return claimsResolver.apply(claims);
  }

  //check expiration
  public boolean isTokenExpired(String token) {
    Date expirationDate = this.extractClaim(token, Claims::getExpiration);
    return expirationDate.before(new Date());
  }

  public String extractPhoneNumber(String token) {
    return extractClaim(token, Claims::getSubject);
  }

  public boolean validateToken(String token, UserDetails userDetails) {
    String phoneNumber = extractPhoneNumber(token);
    return (phoneNumber.equals(userDetails.getUsername())) && !isTokenExpired(token);
  }
}
