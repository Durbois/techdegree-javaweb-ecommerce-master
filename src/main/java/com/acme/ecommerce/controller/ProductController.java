package com.acme.ecommerce.controller;

import static com.acme.ecommerce.util.Utils.formatCartSubTotal;

import com.acme.ecommerce.controller.flashmessage.FlashMessage;
import com.acme.ecommerce.domain.Product;
import com.acme.ecommerce.domain.ProductPurchase;
import com.acme.ecommerce.domain.ShoppingCart;
import com.acme.ecommerce.exception.ProductNotFoundException;
import com.acme.ecommerce.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.activation.MimetypesFileTypeMap;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

@Controller
@RequestMapping("/product")
public class ProductController {

	private static final int INITIAL_PAGE = 0;
	private static final int PAGE_SIZE = 5;
	final Logger logger = LoggerFactory.getLogger(ProductController.class);
	@Autowired
	ProductService productService;

	@Autowired
	HttpSession session;

	@Value("${imagePath:/images/}")
	String imagePath;

	@RequestMapping("/")
	public String index(Model model, @RequestParam(value = "page", required = false) Integer page) {
		logger.debug("Getting Product List");
		logger.debug("Session ID = " + session.getId());

		// Evaluate page. If requested parameter is null or less than 0 (to
		// prevent exception), return initial size. Otherwise, return value of
		// param. decreased by 1.
		int evalPage = (page == null || page < 1) ? INITIAL_PAGE : page - 1;

		Page<Product> products = productService.findAll(new PageRequest(evalPage, PAGE_SIZE));

		model.addAttribute("products", products);
		model.addAttribute("subtotalFormatted", getCartSubTotal());

		return "index";
	}

	@ExceptionHandler(ProductNotFoundException.class)
	public String productNotFoundView () {
		logger.debug("Exception Handler");

		return "product_not_found_page";
	}

	@RequestMapping(path = "/detail/{id}", method = RequestMethod.GET)
	public String productDetail(@PathVariable long id, Model model, RedirectAttributes redirectAttributes) {
		logger.debug("Details for Product " + id);

		Product returnProduct = productService.findById(id);
		if (returnProduct != null) {
			model.addAttribute("product", returnProduct);
			ProductPurchase productPurchase = new ProductPurchase();
			productPurchase.setProduct(returnProduct);
			productPurchase.setQuantity(1);
			model.addAttribute("productPurchase", productPurchase);
		} else {
			logger.error("Product " + id + " Not Found!");
			redirectAttributes.addFlashAttribute("flash",
					new FlashMessage("Product " + id + " Not Found!",
							FlashMessage.Status.FAILURE));

			return "redirect:/product/";
		}

		model.addAttribute("subtotalFormatted", getCartSubTotal());
		return "product_detail";
	}

	@RequestMapping(path="/{id}/image", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<InputStreamResource> productImage(@PathVariable long id) throws FileNotFoundException {
		MimetypesFileTypeMap mimeTypesMap = new MimetypesFileTypeMap();

		logger.debug("Product Image Request for " + id);
		logger.info("Using imagePath [" + imagePath + "]");

		Product returnProduct = productService.findById(id);
		String imageFilePath = null;
		if (returnProduct != null) {
			if (!imagePath.endsWith("/")) {
				imagePath = imagePath + "/";
			}
			imageFilePath = imagePath + returnProduct.getFullImageName();
		}
		File imageFile = new File(imageFilePath);

		return ResponseEntity.ok()
				.contentLength(imageFile.length())
				.contentType(MediaType.parseMediaType(mimeTypesMap.getContentType(imageFile)))
				.body(new InputStreamResource(new FileInputStream(imageFile)));
	}

	@RequestMapping(path = "/about")
	public String aboutCartShop(Model model) {
		model.addAttribute("subtotalFormatted", getCartSubTotal());
		logger.warn("Happy Easter! Someone actually clicked on About.");
		return("about");
	}
	//  subtotal
	public String getCartSubTotal() {

		if (session.getAttribute("shoppingCart")==null){
			return "";
		};

		ShoppingCart shoppingCart =(ShoppingCart)session.getAttribute("shoppingCart");
		String s=formatCartSubTotal(shoppingCart);
		return s;
	}
}
