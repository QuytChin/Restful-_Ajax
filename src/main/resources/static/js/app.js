"use strict";
// AJAX bằng Fetch API. Chuyển dữ liệu JSON mà không tải lại trang.
const $ = id => document.getElementById(id);
const mode = document.body.dataset.mode;
const page = document.body.dataset.page;
const base = (document.body.dataset.base || '/').replace(/\/$/, '');
const url = path => base + path;
const state = {number:0,size:5,totalPages:0,categories:[],editing:null,version:0,busy:false};
const isCategory = page === 'categories';
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
const categoryFields = 'categoryId categoryName icon';
const productFields = `productId productName quantity unitPrice images description discount createDate status category { ${categoryFields} }`;
const pageFields = 'number size totalElements totalPages first last';
function notice(message,success=false) { $('notice').textContent=message; $('notice').className=success?'success':''; $('notice').hidden=false; }
async function request(path,options={}) {
 const response=await fetch(url(path),options);
 if(response.status===204) return null;
 let body;try {body=await response.json();} catch {throw new Error('Máy chủ trả về dữ liệu không hợp lệ. Kiểm tra ứng dụng đã chạy.');}
 if(!response.ok || body.status===false) throw new Error(body.message||`Lỗi HTTP ${response.status}`);
 return body;
}
async function rest(path,method='GET',data) {
 const opts={method}; if(data!==undefined) {opts.headers={'Content-Type':'application/json'};opts.body=JSON.stringify(data);}
 const result=await request(path,opts);return result?.body;
}
async function gql(query,variables={}) {
 const result=await request('/graphql',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({query,variables})});
 if(result.errors?.length) throw new Error(result.errors.map(e=>e.message).join('; '));
 return result.data;
}
const api={
 async categories(keyword='',number=0,size=100) {
  if(mode==='REST') return rest('/api/categories?'+new URLSearchParams({keyword,page:number,size}));
  return (await gql(`query($keyword:String!,$page:Int!,$size:Int!){categories(keyword:$keyword,page:$page,size:$size){${pageFields} content{${categoryFields}}}}`,{keyword,page:number,size})).categories;
 },
 async listProducts(keyword,categoryId,number,size,sort) {
  if(mode==='REST') {const q=new URLSearchParams({keyword,page:number,size,sort});if(categoryId) q.set('categoryId',categoryId);return rest('/api/products?'+q);}
  return (await gql(`query($keyword:String!,$categoryId:ID,$page:Int!,$size:Int!,$sort:String!){products(keyword:$keyword,categoryId:$categoryId,page:$page,size:$size,sort:$sort){${pageFields} content{${productFields}}}}`,{keyword,categoryId:categoryId||null,page:number,size,sort})).products;
 },
 async one(kind,id) {
  if(mode==='REST') return rest(`/api/${kind}/${id}`);
  const field=kind==='categories'?'categoryById':'productById';
  return (await gql(`query($id:ID!){${field}(id:$id){${kind==='categories'?categoryFields:productFields}}}`,{id:String(id)}))[field];
 },
 async save(kind,id,input) {
  if(mode==='REST') return rest(`/api/${kind}${id?'/'+id:''}`,id?'PUT':'POST',input);
  const type=kind==='categories'?'Category':'Product',method=(id?'update':'create')+type;
  const query=id?`mutation($id:ID!,$input:${type}Input!){${method}(id:$id,input:$input){${kind==='categories'?'categoryId':'productId'}}}`:`mutation($input:${type}Input!){${method}(input:$input){${kind==='categories'?'categoryId':'productId'}}}`;
  return (await gql(query,{...(id?{id:String(id)}:{}),input}))[method];
 },
 async remove(kind,id) {
  if(mode==='REST') return rest(`/api/${kind}/${id}`,'DELETE');
  const method=kind==='categories'?'deleteCategory':'deleteProduct';return (await gql(`mutation($id:ID!){${method}(id:$id)}`,{id:String(id)}))[method];
 }
};
async function loadCategories() {
 let all=[],n=0,last=false;
 do {const p=await api.categories('',n++,100);all=all.concat(p.content);last=p.last;} while(!last);
 state.categories=all;$('categoryCount').textContent=all.length;
 const selected=$('categoryFilter').value;
 $('categoryFilter').innerHTML='<option value="">Tất cả danh mục</option>'+all.map(c=>`<option value="${esc(c.categoryId)}">${esc(c.categoryName)}</option>`).join('');
 if(all.some(c=>String(c.categoryId)===selected)) $('categoryFilter').value=selected;
}
function rows(items) {
 $('tableHead').innerHTML=isCategory?'<tr><th>ID</th><th>ẢNH MINH HỌA</th><th>DANH MỤC</th><th>THAO TÁC</th></tr>':'<tr><th>ID</th><th>SẢN PHẨM</th><th>DANH MỤC</th><th>ĐƠN GIÁ</th><th>SL</th><th>TRẠNG THÁI</th><th>THAO TÁC</th></tr>';
 $('rows').innerHTML=items.map(x=>{
  const id=isCategory?x.categoryId:x.productId;
  const actions=`<div class="actions">${isCategory?'':`<button data-detail="${esc(id)}">Xem</button>`}<button data-edit="${esc(id)}">Sửa</button><button class="delete" data-delete="${esc(id)}">Xóa</button></div>`;
  return isCategory?`<tr><td>#${esc(id)}</td><td class="category-image-cell"><img class="category-thumb" src="${categoryImg(x)}" data-fallback="${url(categoryDefault(x.categoryName))}" alt="${esc(x.categoryName)}" width="140" height="100"></td><td class="name">${esc(x.categoryName)}</td><td>${actions}</td></tr>`:`<tr><td>#${esc(id)}</td><td class="name" title="${esc(x.productName)}"><img class="thumb" src="${imgUrl(x.images)}" alt=""><span class="product-copy">${esc(x.productName)}<small>${esc(x.description)}</small></span></td><td>${esc(x.category.categoryName)}</td><td>${money(x.unitPrice)}</td><td>${x.quantity}</td><td><span class="pill ${x.status===1?'':'off'}">${x.status===1?'Đang bán':'Tạm ẩn'}</span></td><td>${actions}</td></tr>`;
 }).join('');
}
async function load() {
 const version=++state.version;$('loading').hidden=false;
 try {
  const keyword=$('keyword').value.trim(),cat=$('categoryFilter').value;
  {
   const p=isCategory?await api.categories(keyword,state.number,state.size):await api.listProducts(keyword,cat,state.number,state.size,$('sort').value);
   if(version!==state.version) return;
   if(p.number>0&&p.content.length===0) {state.number=Math.max(0,p.totalPages-1);return load();}
   state.totalPages=p.totalPages;rows(p.content);$('resultCount').textContent=p.totalElements;
   $('tableWrap').hidden=p.content.length===0;$('empty').hidden=p.content.length!==0;$('pagination').hidden=p.totalPages===0;
   $('pageInfo').textContent=`${p.totalElements===0?0:p.number*p.size+1}–${p.number*p.size+p.content.length} / ${p.totalElements} kết quả`;
   $('pageNumber').textContent=`${p.number+1} / ${p.totalPages}`;$('prev').disabled=p.first;$('next').disabled=p.last;
  }
 } catch(e) {if(version===state.version) notice(e.message);} finally {if(version===state.version) $('loading').hidden=true;}
}
const field=(label,input,full=false)=>{const tag=input.startsWith('<img')?'div':'label';return `<${tag} class="field ${full?'full':''}"><span>${label}</span>${input}</${tag}>`;};
async function edit(id=null) {
 if(state.busy) return;state.busy=true;
 try {
  await loadCategories();
  const x=id?await api.one(page,id):{};state.editing=id;
  $('editorTitle').textContent=(id?'Chỉnh sửa ':'Thêm ')+(isCategory?'danh mục':'sản phẩm');$('formError').hidden=true;
  let f='';
  if(isCategory) f=field('Tên danh mục *',`<input name="categoryName" value="${esc(x.categoryName)}" maxlength="150" required>`,true);
  else {
   f+=field('Tên sản phẩm *',`<input name="productName" value="${esc(x.productName)}" maxlength="500" required>`,true);
   f+=field('Danh mục *',`<select name="categoryId" required><option value="">Chọn danh mục</option>${state.categories.map(c=>`<option value="${esc(c.categoryId)}" ${String(c.categoryId)===String(x.category?.categoryId)?'selected':''}>${esc(c.categoryName)}</option>`).join('')}</select>`);
   f+=field('Đơn giá (VNĐ) *',`<input name="unitPrice" type="number" min="0" max="9999999999999999" step="0.01" value="${esc(x.unitPrice??0)}" required>`);
   f+=field('Số lượng *',`<input name="quantity" type="number" min="0" max="2147483647" step="1" value="${esc(x.quantity??0)}" required>`);
   f+=field('Giảm giá (%) *',`<input name="discount" type="number" min="0" max="100" step="0.01" value="${esc(x.discount??0)}" required>`);
   f+=field('Trạng thái',`<select name="status"><option value="1" ${x.status===0?'':'selected'}>Đang bán</option><option value="0" ${x.status===0?'selected':''}>Tạm ẩn</option></select>`);
   f+=field('Mô tả',`<textarea name="description" maxlength="500">${esc(x.description)}</textarea>`,true);
  }
  f+=field('Ảnh minh họa',`<img class="upload-preview" id="uploadPreview" src="${isCategory?categoryImg(x):imgUrl(x.images)}" alt="Ảnh hiện tại"><input type="file" id="file" aria-label="Chọn ảnh minh họa" accept="image/png,image/jpeg,image/gif"><small>PNG, JPG, GIF · tối đa 5 MB. Không chọn ảnh mới sẽ giữ ảnh hiện tại.</small><label><input type="checkbox" id="removeImage"> Bỏ ảnh hiện tại</label>`,true);
  $('fields').innerHTML='<div class="form-grid">'+f+'</div>';
  let previewUrl=null;
  $('file').onchange=()=>{if(previewUrl) URL.revokeObjectURL(previewUrl);const file=$('file').files[0];if(file) {previewUrl=URL.createObjectURL(file);$('uploadPreview').src=previewUrl;$('removeImage').checked=false;}};
  $('editor').onclose=()=>{if(previewUrl) URL.revokeObjectURL(previewUrl);};
  $('editor').showModal();
 } catch(e) {notice(e.message);} finally {state.busy=false;}
}
$('editForm').addEventListener('submit',async e=>{
 e.preventDefault();if(state.busy) return;state.busy=true;$('saveBtn').disabled=true;$('formError').hidden=true;
 try {
  const data=Object.fromEntries(new FormData(e.target));
  let image=null;const file=$('file').files[0];
  if(file&&!$('removeImage').checked) {if(file.size>5*1024*1024) throw new Error('Ảnh không được vượt quá 5 MB');const form=new FormData();form.append('file',file);image=(await request('/api/files',{method:'POST',body:form})).body.url;}
  else if($('removeImage').checked) image='';
  const input=isCategory?{categoryName:data.categoryName.trim(),icon:image}:{productName:data.productName.trim(),quantity:Number(data.quantity),unitPrice:Number(data.unitPrice),images:image,description:data.description,discount:Number(data.discount),status:Number(data.status),categoryId:data.categoryId};
  await api.save(page,state.editing,input);$('editor').close();notice('Đã lưu thông tin thành công.',true);await loadCategories();await load();
 } catch(error) {$('formError').textContent=error.message;$('formError').hidden=false;}
 finally {state.busy=false;$('saveBtn').disabled=false;}
});
async function remove(id) {
 if(state.busy||!confirm(`Xóa ${isCategory?'danh mục':'sản phẩm'} #${id}?`)) return;
 state.busy=true;
 try {await api.remove(page,id);notice('Đã xóa thành công.',true);await loadCategories();await load();} catch(e) {notice(e.message);} finally {state.busy=false;}
}
async function showDetail(id) {
 try {const x=await api.one('products',id);
  const parts=[['Tên sản phẩm',x.productName],['Mã sản phẩm','#'+x.productId],['Danh mục',x.category.categoryName],['Đơn giá',money(x.unitPrice)],['Số lượng',x.quantity],['Giảm giá',x.discount+'%'],['Trạng thái',x.status===1?'Đang bán':'Tạm ẩn'],['Ngày tạo',x.createDate.replace('T',' ').slice(0,19)],['Mô tả',x.description||'Chưa có mô tả']];
  $('detailContent').innerHTML=`<img class="detail-image" src="${imgUrl(x.images)}" alt="${esc(x.productName)}"><dl class="detail-grid">${parts.map(([k,v])=>`<dt>${esc(k)}</dt><dd>${esc(v)}</dd>`).join('')}</dl>`;
  $('detail').showModal();
 } catch(e) {notice(e.message);}
}
document.addEventListener('click',e=>{const b=e.target.closest('button');if(!b)return;if(b.dataset.edit) edit(b.dataset.edit);if(b.dataset.delete) remove(b.dataset.delete);if(b.dataset.detail) showDetail(b.dataset.detail);});
document.addEventListener('error',e=>{const img=e.target;if(img.tagName!=='IMG')return;if(img.dataset.fallback){const fallback=img.dataset.fallback;delete img.dataset.fallback;if(img.getAttribute('src')!==fallback){img.src=fallback;return;}}if(!img.src.endsWith('/images/product.svg'))img.src=url('/images/product.svg');},true);
$('closeEditor').onclick=$('cancelEditor').onclick=()=>{if(!state.busy)$('editor').close();};
$('editor').addEventListener('cancel',e=>{if(state.busy)e.preventDefault();});
$('closeDetail').onclick=()=>$('detail').close();
$('addBtn').onclick=()=>edit();
let timer;
$('keyword').addEventListener('input',()=>{clearTimeout(timer);state.version++;timer=setTimeout(()=>{state.number=0;load();},300);});
for(const id of ['categoryFilter','sort','pageSize']) $(id).onchange=()=>{state.number=0;state.size=Number($('pageSize').value);load();};
$('prev').onclick=()=>{if(state.number>0){state.number--;load();}};
$('next').onclick=()=>{if(state.number<state.totalPages-1){state.number++;load();}};
$('refresh').onclick=async()=>{try {await loadCategories();await load();}catch(e){notice(e.message);}};
async function init() {
 document.querySelector(`[data-nav="${page}"]`)?.classList.add('active');
 const title=page==='home'?'Khám phá sản phẩm':isCategory?'Quản lý danh mục':'Quản lý sản phẩm';
 $('pageTitle').textContent=title;$('crumb').textContent=title;document.title=title+' | NOVA Desk';
 $('addBtn').hidden=page==='home';$('addBtn').textContent=isCategory?'+ Thêm danh mục':'+ Thêm sản phẩm';
 $('categoryFilter').hidden=isCategory;$('sort').hidden=isCategory||page==='home';$('pageSize').hidden=page==='home';
 $('keyword').placeholder=isCategory?'Tìm theo tên danh mục…':'Tìm theo tên sản phẩm…';$('countLabel').textContent=isCategory?'Danh mục phù hợp':'Sản phẩm phù hợp';
 $('subtitle').hidden=page!=='home';
 if(page==='home') $('subtitle').textContent='Giá từ thấp đến cao';
 try {await loadCategories(); const params=new URLSearchParams(window.location.search); if(page==='products'){const id=params.get('categoryId');if(id&&state.categories.some(c=>String(c.categoryId)===id))$('categoryFilter').value=id; $('keyword').value=params.get('keyword')||'';} await load();} catch(e) {notice(e.message);}
}
init();
