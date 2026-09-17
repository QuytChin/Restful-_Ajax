"use strict";
const $=id=>document.getElementById(id);
const base=(document.body.dataset.base||'/').replace(/\/$/,'');
const url=path=>base+path;
const money = v => new Intl.NumberFormat('vi-VN',{style:'currency',currency:'VND',maximumFractionDigits:0}).format(v);
const esc = v => String(v ?? '').replace(/[&<>"']/g, ch => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[ch]));
const packagedImages = new Set(["/images/products/laptop-study.svg", "/images/products/laptop-creative.svg", "/images/products/phone-a1.svg", "/images/products/phone-b2.svg", "/images/products/keyboard.svg", "/images/products/mouse.svg", "/images/products/headphones.svg", "/images/products/speaker.svg", "/images/products/monitor-24.svg", "/images/products/monitor-27.svg", "/images/products/ssd-500.svg", "/images/products/usb-64.svg", "/images/categories/laptops.svg", "/images/categories/phones.svg", "/images/categories/accessories.svg", "/images/categories/audio.svg", "/images/categories/monitors.svg", "/images/categories/storage.svg", "/images/categories/laptops.png", "/images/categories/phones.png", "/images/categories/accessories.png", "/images/categories/audio.png", "/images/categories/monitors.png", "/images/categories/storage.png", "/images/categories/default.png"]);
const imgUrl = value => url(packagedImages.has(value) ? value : /^\/uploads\/[a-f0-9-]+\.(png|jpg|jpeg|gif)$/.test(value || '') ? value : '/images/product.svg');
// Ảnh dự phòng không phụ thuộc vào việc seed dữ liệu đã chạy hay chưa.
function categoryDefault(name) {
 const normalized=String(name||'').normalize('NFD').replace(/[\u0300-\u036f]/g,'').replace(/đ/gi,'d').toLowerCase().replace(/\s+/g,' ').trim();
 const groups={laptops:['laptop','may tinh xach tay'],phones:['dien thoai','smartphone'],accessories:['phu kien'],audio:['am thanh','loa','tai nghe'],monitors:['man hinh','monitor'],storage:['thiet bi luu tru','luu tru','o cung','usb']};
 const group=Object.keys(groups).find(k=>groups[k].some(n=>normalized===n));
 return '/images/categories/'+(group||'default')+'.png';
}
function categoryImg(c) {
 const value=String(c.icon||'').trim();
 if(value.startsWith('/images/categories/') && packagedImages.has(value)) return url(value.replace(/\.svg$/,'.png'));
 if(packagedImages.has(value)||/^\/uploads\/[a-f0-9-]+\.(png|jpg|jpeg|gif)$/.test(value)) return imgUrl(value);
 return url(categoryDefault(c.categoryName));
}

async function get(path) {
 const response=await fetch(url(path));
 let result;try{result=await response.json();}catch{throw new Error('Không đọc được dữ liệu. Hãy kiểm tra ứng dụng đã chạy.');}
 if(!response.ok||result.status===false) throw new Error(result.message||'Không tải được dữ liệu');
 return result.body;
}
async function categories() {
 let number=0,all=[],page;
 do {page=await get('/api/categories?page='+number+'&size=100');all.push(...page.content);number++;}while(!page.last);
 return all;
}
async function loadHome() {
 $('refreshHome').disabled=true;$('homeNotice').hidden=true;
 try {
  const [cats,products]=await Promise.all([categories(),get('/api/products?page=0&size=6&sort=id')]);
  $('homeCategories').textContent=cats.length;$('homeProducts').textContent=products.totalElements;
  $('homeCategoryGrid').innerHTML=cats.length?cats.map(c=>`<a class="dashboard-category" href="${url('/admin/products')}?categoryId=${encodeURIComponent(c.categoryId)}"><img src="${categoryImg(c)}" data-fallback="${url(categoryDefault(c.categoryName))}" alt="${esc(c.categoryName)}" width="180" height="129"><span>${esc(c.categoryName)}</span></a>`).join(''):'<p class="dashboard-empty">Chưa có danh mục. Chọn Quản lý danh mục để thêm mới.</p>';
  $('homeProductGrid').innerHTML=products.content.length?products.content.map(p=>`<a class="dashboard-product" href="${url('/admin/products')}?keyword=${encodeURIComponent(p.productName)}"><img src="${imgUrl(p.images)}" alt="${esc(p.productName)}" width="220" height="157"><div><small>${esc(p.category.categoryName)}</small><h3>${esc(p.productName)}</h3><strong>${money(p.unitPrice)}</strong><span>${p.status===1?'Đang bán':'Tạm ẩn'} · ${p.quantity} sản phẩm</span></div></a>`).join(''):'<p class="dashboard-empty">Chưa có sản phẩm. Chọn Xem tất cả để thêm mới.</p>';
 }catch(e){$('homeNotice').textContent=e.message;$('homeNotice').hidden=false;}finally{$('refreshHome').disabled=false;}
}
document.addEventListener('error',e=>{const img=e.target;if(img.tagName!=='IMG')return;if(img.dataset.fallback){const fallback=img.dataset.fallback;delete img.dataset.fallback;if(img.getAttribute('src')!==fallback){img.src=fallback;return;}}if(!img.src.endsWith('/images/product.svg'))img.src=url('/images/product.svg');},true);
$('refreshHome').onclick=loadHome;
loadHome();
