package dev.damda.member.api;

import dev.damda.member.domain.Role;
import java.util.Set;

public record MeResponse(Long id, String email, String nickname, Set<Role> roles) {}
