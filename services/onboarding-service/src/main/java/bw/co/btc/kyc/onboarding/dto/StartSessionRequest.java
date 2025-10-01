package bw.co.btc.kyc.onboarding.dto;

import bw.co.btc.kyc.onboarding.enumeration.Channel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record StartSessionRequest (
    @NotNull Channel channel,
    @NotBlank @Pattern(regexp = "^\\+?[0-9]{7,15}$") String phone
){}
