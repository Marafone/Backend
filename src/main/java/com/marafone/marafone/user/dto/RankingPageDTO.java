package com.marafone.marafone.user.dto;

import java.util.List;

public record RankingPageDTO(List<UserRankingDTO> users, int pageNumber) {
}