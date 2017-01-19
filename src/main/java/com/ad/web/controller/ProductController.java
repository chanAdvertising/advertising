package com.ad.web.controller;

import com.ad.dao.ProductDao;
import com.ad.dao.ProductDetailDao;
import com.ad.entity.PageProductParam;
import com.ad.entity.Product;
import com.ad.entity.ProductDetail;
import com.ad.service.UploadService;
import com.ad.util.PaginationHelper;
import com.google.common.base.Preconditions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/ad")
public class ProductController {

    private final ProductDao productDao;

    @Autowired
    private ProductDetailDao productDetailDao;
    @Autowired
    private UploadService uploadService;

    @Autowired
    public ProductController(ProductDao productDao) {
        this.productDao = productDao;
    }

    @RequestMapping(value = "/product-list/{id}", method = RequestMethod.GET)
    public String product(@PathVariable(value = "id") Integer id, Model model) {
        List<Product> list = productDao.findByParentId(id);
        Preconditions.checkState(!list.isEmpty(), String.format("未找到id为[%s]的产品.", id));
        model.addAttribute("products", list);
        return "product";
    }

    @RequestMapping(value = "/product-list/detail", method = RequestMethod.POST)
    public String detail(@RequestParam("id") Integer id, Model model) {
        List<Product> list = productDao.findByParentId(id);
        Preconditions.checkState(!list.isEmpty(), String.format("未找到id为[%s]的产品.", id));
        model.addAttribute("list", list);
        model.addAttribute("parent", productDao.findOneById(list.get(0).getParentId()));
        return "tpl/detail";
    }

    @RequestMapping(value = "/product/list", method = RequestMethod.GET)
    public String list(Model model, @RequestParam(value = "page",
            required = false,
            defaultValue = "1") Integer page) {
        Page<ProductDetail> productDetail = productDetailDao.findAll(new PageRequest(page - 1, 1));
        model.addAttribute("page", new PaginationHelper<ProductDetail>(productDetail, "/ad/product/list"));
        return "admin/product_list";
    }

    @RequestMapping(value = "/product/add", method = RequestMethod.GET)
    public String add() {
        return "admin/new";
    }

    @RequestMapping(value = "/product/add", method = RequestMethod.POST)
    public String addProduct(PageProductParam param, RedirectAttributes redirectAttributes) {

        try {
            Product product = new Product();
            ProductDetail detail = new ProductDetail();
            product.setLevel(2);
            product.setName(param.getName());
            product.setParentId(param.getParentId());
            product.setImgUrl("/products/" + param.getImg().getOriginalFilename());
            uploadService.upload(param.getImg());
            detail.setMaterial(param.getMeaterial());
            detail.setModel(param.getModel());
            detail.setRemark(param.getRemark());
            detail.setSize(param.getSize());
            detail.setDetailImg("/products/" + param.getDetail().getOriginalFilename());
            uploadService.upload(param.getDetail());
            detail.setProduct(product);
            product.setDetail(detail);
            productDao.save(product);
            redirectAttributes.addFlashAttribute("message", "上传产品成功");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", String.format("上传产品失败[%s]", e.getMessage()));
        }
        return "redirect:/ad/admin/index";
    }

    @RequestMapping(value = "/product-list/detail/{id}", method = RequestMethod.GET)
    public String getDetail(@PathVariable("id") Integer id, Model model) {
        ProductDetail detail = productDetailDao.findOneByProductId(id);
        Preconditions.checkNotNull(detail, "未找到商品详情");
        model.addAttribute("detail", detail);
        return "product_detail";
    }

    @RequestMapping(value = "/product-list/shopping", method = RequestMethod.GET)
    public String shoppingList() {
        return "";
    }

    @RequestMapping(value = "/product-list/shopping", method = RequestMethod.POST)
    public String shopping() {
        return "";
    }
}
