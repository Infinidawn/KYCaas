package bw.co.btc.kyc.document.repo;

import bw.co.btc.kyc.document.entity.DocumentSubmission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DocumentSubmissionRepository extends JpaRepository<DocumentSubmission, UUID> {}
