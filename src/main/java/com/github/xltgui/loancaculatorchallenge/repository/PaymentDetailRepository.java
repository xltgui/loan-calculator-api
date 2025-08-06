package com.github.xltgui.loancaculatorchallenge.repository;

import com.github.xltgui.loancaculatorchallenge.model.PaymentDetail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentDetailRepository extends JpaRepository<PaymentDetail, Long> {
}
