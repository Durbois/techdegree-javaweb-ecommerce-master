package com.acme.ecommerce.service;

import com.acme.ecommerce.domain.ProductPurchase;
import com.acme.ecommerce.domain.Purchase;
import com.acme.ecommerce.exception.ExceedException;
import com.acme.ecommerce.repository.PurchaseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Service
public class PurchaseServiceImpl implements PurchaseService {

	private final PurchaseRepository repository;

	private static int PRODUCT_MAX = 10;

	@Autowired
	public PurchaseServiceImpl(PurchaseRepository repository) {
		this.repository = repository;
	}

	@Override
	public Iterable<Purchase> findAll() {
		return repository.findAll();
	}

	@Override
	public Purchase findById(Long id) {
		Purchase result = repository.findOne(id);

		return result;
	}

	@Override
	public Purchase save(Purchase purchase) {

		for (ProductPurchase pp : purchase.getProductPurchases()) {
			if (pp.getQuantity() > PRODUCT_MAX) {
				throw new ExceedException("Exceed Exception");
			}
		}

		Purchase result = repository.save(purchase);

		return result;
	}

}
