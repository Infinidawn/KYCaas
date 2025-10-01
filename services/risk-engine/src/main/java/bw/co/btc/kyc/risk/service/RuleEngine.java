package bw.co.btc.kyc.risk.service;


import bw.co.btc.kyc.risk.entity.VerificationSignal;
import bw.co.btc.kyc.risk.enumeration.KycDecisionStatus;
import bw.co.btc.kyc.risk.enumeration.SignalStatus;

import java.util.*;

import static bw.co.btc.kyc.risk.enumeration.KycDecisionStatus.*;


public class RuleEngine {

    /**
     * Simple, extensible aggregation:
     * - Any FAIL ⇒ REJECTED (collect reasons)
     * - If DOCUMENT:PASS & BIOMETRIC:PASS ⇒ APPROVED
     * - Else ⇒ REVIEW
     */
    public Result evaluate(List<VerificationSignal> signals) {
        boolean docPass=false, bioPass=false;
        List<String> reasons = new ArrayList<>();

        for (var s: signals) {
            if (s.getStatus() == SignalStatus.FAIL) {
                reasons.add(s.getSource().name()+"_FAIL");
            }
            if (s.getSource().name().equals("DOCUMENT") && s.getStatus()==SignalStatus.PASS) docPass = true;
            if (s.getSource().name().equals("BIOMETRIC") && s.getStatus()==SignalStatus.PASS) bioPass = true;
        }

        if (reasons.stream().anyMatch(r -> r.endsWith("_FAIL"))) return new Result(REJECTED, reasons);
        if (docPass && bioPass) return new Result(APPROVED, List.of());
        return new Result(REVIEW, reasons.isEmpty()? List.of("INSUFFICIENT_EVIDENCE") : reasons);
    }

    public record Result(KycDecisionStatus status, List<String> reasons){}
}