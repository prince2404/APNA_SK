import { useState, useEffect } from 'react';
import { Package, Tags, Search, Plus, Edit, Power, Percent, BarChart3, AlertCircle } from 'lucide-react';
import { PageHeader } from '@/components/common/PageHeader';
import { productApi } from '@/api/productApi';
import { toast } from '@/store/useNotificationStore';
import { usePermission } from '@/hooks/usePermission';
import { Modal } from '@/components/common/Modal';
import { Input } from '@/components/common/Input';
import { Button } from '@/components/common/Button';
import { Pagination } from '@/components/common/Pagination';
import { StatusBadge } from '@/components/common/StatusBadge';
import { ROLES } from '@/constants/roles';

export default function ProductsPage() {
  const { isSuperAdmin, hasRole, hasPermission } = usePermission();
  const canManageCatalog = isSuperAdmin || hasRole(ROLES.SYSTEM_ADMIN);

  // Tabs
  const [activeTab, setActiveTab] = useState('products'); // 'products' | 'categories'

  // Products State
  const [products, setProducts] = useState([]);
  const [productTotal, setProductTotal] = useState(0);
  const [productPage, setProductPage] = useState(0);
  const [productSize] = useState(10);
  const [productSearch, setProductSearch] = useState('');
  const [selectedCategoryFilter, setSelectedCategoryFilter] = useState('');
  const [loadingProducts, setLoadingProducts] = useState(false);

  // Categories State
  const [categories, setCategories] = useState([]);
  const [loadingCategories, setLoadingCategories] = useState(false);

  // Modals
  const [isProductModalOpen, setIsProductModalOpen] = useState(false);
  const [isCategoryModalOpen, setIsCategoryModalOpen] = useState(false);
  const [editingProduct, setEditingProduct] = useState(null);

  // Product Form State
  const [productForm, setProductForm] = useState({
    name: '',
    brand: '',
    categoryId: '',
    hsnCode: '',
    mrp: '',
    askPrice: '',
    gstPercentage: '18.00',
    minStockThreshold: '10',
    description: '',
  });
  const [submittingProduct, setSubmittingProduct] = useState(false);

  // Category Form State
  const [categoryForm, setCategoryForm] = useState({ name: '', description: '' });
  const [submittingCategory, setSubmittingCategory] = useState(false);

  // Load Data
  const fetchCategories = async () => {
    setLoadingCategories(true);
    try {
      const res = await productApi.getCategories();
      setCategories(res.data.data || []);
    } catch (err) {
      toast.error('Failed to load categories');
    } finally {
      setLoadingCategories(false);
    }
  };

  const fetchProducts = async (page = 0) => {
    setLoadingProducts(true);
    try {
      const params = {
        page,
        size: productSize,
        search: productSearch || undefined,
        categoryId: selectedCategoryFilter || undefined,
      };
      const res = await productApi.getProducts(params);
      setProducts(res.data.data?.content || []);
      setProductTotal(res.data.data?.totalElements || 0);
      setProductPage(page);
    } catch (err) {
      toast.error('Failed to load products catalogue');
    } finally {
      setLoadingProducts(false);
    }
  };

  useEffect(() => {
    fetchCategories();
  }, []);

  useEffect(() => {
    fetchProducts(0);
  }, [productSearch, selectedCategoryFilter]);

  // Actions
  const handleProductSubmit = async (e) => {
    e.preventDefault();
    const mrpValue = parseFloat(productForm.mrp);
    const askValue = parseFloat(productForm.askPrice);

    if (!productForm.name || !productForm.brand || !productForm.categoryId || !productForm.hsnCode) {
      toast.error('Please fill in all required fields');
      return;
    }
    if (isNaN(mrpValue) || mrpValue <= 0) {
      toast.error('MRP must be greater than 0');
      return;
    }
    if (isNaN(askValue) || askValue <= 0) {
      toast.error('ASK Price must be greater than 0');
      return;
    }
    if (askValue > mrpValue) {
      toast.error('ASK Price must be less than or equal to MRP');
      return;
    }

    setSubmittingProduct(true);
    try {
      const payload = {
        ...productForm,
        categoryId: parseInt(productForm.categoryId),
        mrp: mrpValue,
        askPrice: askValue,
        gstPercentage: parseFloat(productForm.gstPercentage),
        minStockThreshold: parseInt(productForm.minStockThreshold),
      };

      if (editingProduct) {
        await productApi.updateProduct(editingProduct.id, payload);
        toast.success('Product updated successfully');
      } else {
        await productApi.createProduct(payload);
        toast.success('Product created successfully');
      }
      setIsProductModalOpen(false);
      fetchProducts(productPage);
    } catch (err) {
      toast.error(err.response?.data?.message || 'Error saving product');
    } finally {
      setSubmittingProduct(false);
    }
  };

  const handleCategorySubmit = async (e) => {
    e.preventDefault();
    if (!categoryForm.name) {
      toast.error('Category name is required');
      return;
    }

    setSubmittingCategory(true);
    try {
      await productApi.createCategory(categoryForm);
      toast.success('Category created successfully');
      setIsCategoryModalOpen(false);
      setCategoryForm({ name: '', description: '' });
      fetchCategories();
    } catch (err) {
      toast.error(err.response?.data?.message || 'Error saving category');
    } finally {
      setSubmittingCategory(false);
    }
  };

  const handleToggleProduct = async (id) => {
    try {
      await productApi.toggleProduct(id);
      toast.success('Product status updated');
      fetchProducts(productPage);
    } catch (err) {
      toast.error('Failed to toggle product status');
    }
  };

  const handleToggleCategory = async (id) => {
    try {
      await productApi.toggleCategory(id);
      toast.success('Category status updated');
      fetchCategories();
      fetchProducts(productPage);
    } catch (err) {
      toast.error('Failed to toggle category status');
    }
  };

  const openProductForm = (product = null) => {
    if (product) {
      setEditingProduct(product);
      setProductForm({
        name: product.name,
        brand: product.brand,
        categoryId: product.category?.id || '',
        hsnCode: product.hsnCode,
        mrp: product.mrp.toString(),
        askPrice: product.askPrice.toString(),
        gstPercentage: product.gstPercentage.toString(),
        minStockThreshold: product.minStockThreshold.toString(),
        description: product.description || '',
      });
    } else {
      setEditingProduct(null);
      setProductForm({
        name: '',
        brand: '',
        categoryId: '',
        hsnCode: '',
        mrp: '',
        askPrice: '',
        gstPercentage: '18.00',
        minStockThreshold: '10',
        description: '',
      });
    }
    setIsProductModalOpen(true);
  };

  return (
    <div className="animate-fade-in space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <PageHeader title="Catalogue & Products" description="Manage platform medicine catalog, categories, pricing guidelines, and thresholds." />
        <div className="flex items-center gap-2">
          {canManageCatalog && (
            <>
              <Button variant="outline" size="sm" onClick={() => setIsCategoryModalOpen(true)}>
                <Plus className="w-4 h-4 mr-1" /> Category
              </Button>
              <Button size="sm" onClick={() => openProductForm(null)}>
                <Plus className="w-4 h-4 mr-1" /> Add Product
              </Button>
            </>
          )}
        </div>
      </div>

      {/* Tabs */}
      <div className="border-b border-surface-200">
        <div className="flex gap-4">
          <button
            onClick={() => setActiveTab('products')}
            className={`py-3 px-4 text-sm font-semibold border-b-2 transition-all flex items-center gap-2 cursor-pointer ${
              activeTab === 'products' ? 'border-primary-600 text-primary-600' : 'border-transparent text-surface-500 hover:text-surface-800'
            }`}
          >
            <Package className="w-4 h-4" /> Products ({productTotal})
          </button>
          <button
            onClick={() => setActiveTab('categories')}
            className={`py-3 px-4 text-sm font-semibold border-b-2 transition-all flex items-center gap-2 cursor-pointer ${
              activeTab === 'categories' ? 'border-primary-600 text-primary-600' : 'border-transparent text-surface-500 hover:text-surface-800'
            }`}
          >
            <Tags className="w-4 h-4" /> Categories ({categories.length})
          </button>
        </div>
      </div>

      {activeTab === 'products' ? (
        <div className="space-y-4">
          {/* Filters */}
          <div className="bg-white rounded-xl border border-surface-200/60 p-4 shadow-sm flex flex-col md:flex-row md:items-center justify-between gap-4">
            <div className="relative flex-1 max-w-md">
              <Search className="absolute left-3 top-3 w-4 h-4 text-surface-400" />
              <input
                type="text"
                placeholder="Search products by name or brand..."
                value={productSearch}
                onChange={(e) => setProductSearch(e.target.value)}
                className="w-full pl-9 pr-4 py-2 text-sm rounded-lg border border-surface-300 focus:outline-none focus:ring-2 focus:ring-primary-500/40 focus:border-primary-500"
              />
            </div>

            <div className="flex items-center gap-3">
              <select
                value={selectedCategoryFilter}
                onChange={(e) => setSelectedCategoryFilter(e.target.value)}
                className="px-3 py-2 text-sm rounded-lg border border-surface-300 bg-white focus:outline-none focus:ring-2 focus:ring-primary-500/40 focus:border-primary-500"
              >
                <option value="">All Categories</option>
                {categories.map((c) => (
                  <option key={c.id} value={c.id}>
                    {c.name}
                  </option>
                ))}
              </select>
            </div>
          </div>

          {/* Products Table */}
          <div className="bg-white rounded-xl border border-surface-200/60 shadow-sm overflow-hidden">
            <div className="overflow-x-auto">
              <table className="w-full text-left border-collapse">
                <thead>
                  <tr className="bg-surface-50 border-b border-surface-200 text-xs font-semibold text-surface-500 uppercase tracking-wider">
                    <th className="px-6 py-4">Product Details</th>
                    <th className="px-6 py-4">Category</th>
                    <th className="px-6 py-4">HSN Code</th>
                    <th className="px-6 py-4 text-right">MRP</th>
                    <th className="px-6 py-4 text-right">ASK Price</th>
                    <th className="px-6 py-4 text-center">Threshold</th>
                    <th className="px-6 py-4 text-center">Status</th>
                    {canManageCatalog && <th className="px-6 py-4 text-right">Actions</th>}
                  </tr>
                </thead>
                <tbody className="divide-y divide-surface-200 text-sm">
                  {loadingProducts ? (
                    <tr>
                      <td colSpan={canManageCatalog ? 8 : 7} className="px-6 py-10 text-center text-surface-400">
                        Loading products...
                      </td>
                    </tr>
                  ) : products.length === 0 ? (
                    <tr>
                      <td colSpan={canManageCatalog ? 8 : 7} className="px-6 py-10 text-center text-surface-400">
                        No products found matching filters.
                      </td>
                    </tr>
                  ) : (
                    products.map((p) => {
                      const discount = p.mrp > 0 ? ((p.mrp - p.askPrice) / p.mrp) * 100 : 0;
                      return (
                        <tr key={p.id} className="hover:bg-surface-50/50 transition-colors">
                          <td className="px-6 py-4">
                            <div className="font-semibold text-surface-900">{p.name}</div>
                            <div className="text-xs text-surface-500">{p.brand}</div>
                          </td>
                          <td className="px-6 py-4">
                            <span className="inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-surface-100 text-surface-800">
                              {p.category?.name || '—'}
                            </span>
                          </td>
                          <td className="px-6 py-4 font-mono text-xs">{p.hsnCode || '—'}</td>
                          <td className="px-6 py-4 text-right font-semibold text-surface-700">₹{p.mrp.toFixed(2)}</td>
                          <td className="px-6 py-4 text-right">
                            <div className="font-bold text-emerald-600">₹{p.askPrice.toFixed(2)}</div>
                            {discount > 0 && (
                              <div className="text-[10px] font-semibold text-emerald-500">Save {discount.toFixed(0)}%</div>
                            )}
                          </td>
                          <td className="px-6 py-4 text-center font-semibold text-surface-700">{p.minStockThreshold}</td>
                          <td className="px-6 py-4 text-center">
                            <StatusBadge status={p.status} />
                          </td>
                          {canManageCatalog && (
                            <td className="px-6 py-4 text-right">
                              <div className="flex justify-end gap-2">
                                <button
                                  onClick={() => openProductForm(p)}
                                  className="p-1.5 rounded-lg text-surface-400 hover:text-primary-600 hover:bg-primary-50 transition-colors cursor-pointer"
                                  title="Edit Product"
                                >
                                  <Edit className="w-4 h-4" />
                                </button>
                                <button
                                  onClick={() => handleToggleProduct(p.id)}
                                  className={`p-1.5 rounded-lg transition-colors cursor-pointer ${
                                    p.status === 'ACTIVE'
                                      ? 'text-danger-500 hover:bg-danger-50'
                                      : 'text-emerald-500 hover:bg-emerald-50'
                                  }`}
                                  title={p.status === 'ACTIVE' ? 'Deactivate Product' : 'Activate Product'}
                                >
                                  <Power className="w-4 h-4" />
                                </button>
                              </div>
                            </td>
                          )}
                        </tr>
                      );
                    })
                  )}
                </tbody>
              </table>
            </div>

            {productTotal > productSize && (
              <div className="px-6 py-4 border-t border-surface-200">
                <Pagination
                  currentPage={productPage + 1}
                  totalPages={Math.ceil(productTotal / productSize)}
                  onPageChange={(page) => fetchProducts(page - 1)}
                />
              </div>
            )}
          </div>
        </div>
      ) : (
        /* Categories Tab */
        <div className="bg-white rounded-xl border border-surface-200/60 shadow-sm overflow-hidden">
          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse">
              <thead>
                <tr className="bg-surface-50 border-b border-surface-200 text-xs font-semibold text-surface-500 uppercase tracking-wider">
                  <th className="px-6 py-4">Category Name</th>
                  <th className="px-6 py-4">Description</th>
                  <th className="px-6 py-4 text-center">Status</th>
                  {canManageCatalog && <th className="px-6 py-4 text-right">Actions</th>}
                </tr>
              </thead>
              <tbody className="divide-y divide-surface-200 text-sm">
                {loadingCategories ? (
                  <tr>
                    <td colSpan={4} className="px-6 py-10 text-center text-surface-400">
                      Loading categories...
                    </td>
                  </tr>
                ) : categories.length === 0 ? (
                  <tr>
                    <td colSpan={4} className="px-6 py-10 text-center text-surface-400">
                      No categories registered yet.
                    </td>
                  </tr>
                ) : (
                  categories.map((c) => (
                    <tr key={c.id} className="hover:bg-surface-50/50 transition-colors">
                      <td className="px-6 py-4 font-semibold text-surface-900">{c.name}</td>
                      <td className="px-6 py-4 text-surface-500">{c.description || '—'}</td>
                      <td className="px-6 py-4 text-center">
                        <StatusBadge status={c.status} />
                      </td>
                      {canManageCatalog && (
                        <td className="px-6 py-4 text-right">
                          <button
                            onClick={() => handleToggleCategory(c.id)}
                            className={`p-1.5 rounded-lg transition-colors cursor-pointer ${
                              c.status === 'ACTIVE'
                                ? 'text-danger-500 hover:bg-danger-50'
                                : 'text-emerald-500 hover:bg-emerald-50'
                            }`}
                            title={c.status === 'ACTIVE' ? 'Deactivate Category' : 'Activate Category'}
                          >
                            <Power className="w-4 h-4" />
                          </button>
                        </td>
                      )}
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* Product Form Modal */}
      <Modal
        isOpen={isProductModalOpen}
        onClose={() => setIsProductModalOpen(false)}
        title={editingProduct ? 'Edit Product Details' : 'Add Product to Catalogue'}
        size="lg"
      >
        <form onSubmit={handleProductSubmit} className="space-y-4">
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <Input
              label="Product Name *"
              required
              placeholder="e.g. Paracetamol 650mg"
              value={productForm.name}
              onChange={(e) => setProductForm({ ...productForm, name: e.target.value })}
            />
            <Input
              label="Brand / Manufacturer *"
              required
              placeholder="e.g. Cipla"
              value={productForm.brand}
              onChange={(e) => setProductForm({ ...productForm, brand: e.target.value })}
            />
            <div>
              <label className="block text-sm font-medium text-surface-700 mb-1.5">
                Category *
              </label>
              <select
                required
                value={productForm.categoryId}
                onChange={(e) => setProductForm({ ...productForm, categoryId: e.target.value })}
                className="w-full px-3.5 py-2.5 text-sm rounded-lg border border-surface-300 bg-white focus:outline-none focus:ring-2 focus:ring-primary-500/40 focus:border-primary-500"
              >
                <option value="">Select Category</option>
                {categories.filter(c => c.status === 'ACTIVE').map((c) => (
                  <option key={c.id} value={c.id}>
                    {c.name}
                  </option>
                ))}
              </select>
            </div>
            <Input
              label="HSN Code *"
              required
              placeholder="e.g. 300490"
              value={productForm.hsnCode}
              onChange={(e) => setProductForm({ ...productForm, hsnCode: e.target.value })}
            />
            <Input
              label="MRP (Maximum Retail Price) *"
              type="number"
              step="0.01"
              min="0.01"
              required
              placeholder="₹0.00"
              value={productForm.mrp}
              onChange={(e) => setProductForm({ ...productForm, mrp: e.target.value })}
            />
            <Input
              label="ASK Price (Apna Swasthya Kendra price) *"
              type="number"
              step="0.01"
              min="0.01"
              required
              placeholder="₹0.00"
              value={productForm.askPrice}
              onChange={(e) => setProductForm({ ...productForm, askPrice: e.target.value })}
            />
            <Input
              label="GST Percentage (%) *"
              type="number"
              step="0.01"
              min="0"
              required
              value={productForm.gstPercentage}
              onChange={(e) => setProductForm({ ...productForm, gstPercentage: e.target.value })}
            />
            <Input
              label="Min Stock Threshold *"
              type="number"
              min="0"
              required
              value={productForm.minStockThreshold}
              onChange={(e) => setProductForm({ ...productForm, minStockThreshold: e.target.value })}
            />
          </div>

          <div className="space-y-1.5">
            <label className="block text-sm font-medium text-surface-700">
              Description / Notes
            </label>
            <textarea
              rows={3}
              placeholder="Enter product description, dosage rules, or packaging details..."
              value={productForm.description}
              onChange={(e) => setProductForm({ ...productForm, description: e.target.value })}
              className="w-full px-3.5 py-2 text-sm rounded-lg border border-surface-300 bg-white focus:outline-none focus:ring-2 focus:ring-primary-500/40 focus:border-primary-500"
            />
          </div>

          <div className="flex justify-end gap-3 pt-4 border-t border-surface-200">
            <Button variant="secondary" onClick={() => setIsProductModalOpen(false)}>
              Cancel
            </Button>
            <Button type="submit" loading={submittingProduct}>
              {editingProduct ? 'Save Changes' : 'Add Product'}
            </Button>
          </div>
        </form>
      </Modal>

      {/* Category Form Modal */}
      <Modal
        isOpen={isCategoryModalOpen}
        onClose={() => setIsCategoryModalOpen(false)}
        title="Add Product Category"
      >
        <form onSubmit={handleCategorySubmit} className="space-y-4">
          <Input
            label="Category Name *"
            required
            placeholder="e.g. Medical Devices"
            value={categoryForm.name}
            onChange={(e) => setCategoryForm({ ...categoryForm, name: e.target.value })}
          />
          <div className="space-y-1.5">
            <label className="block text-sm font-medium text-surface-700">
              Description
            </label>
            <textarea
              rows={3}
              placeholder="Describe the category..."
              value={categoryForm.description}
              onChange={(e) => setCategoryForm({ ...categoryForm, description: e.target.value })}
              className="w-full px-3.5 py-2 text-sm rounded-lg border border-surface-300 bg-white focus:outline-none focus:ring-2 focus:ring-primary-500/40 focus:border-primary-500"
            />
          </div>

          <div className="flex justify-end gap-3 pt-4 border-t border-surface-200">
            <Button variant="secondary" onClick={() => setIsCategoryModalOpen(false)}>
              Cancel
            </Button>
            <Button type="submit" loading={submittingCategory}>
              Create Category
            </Button>
          </div>
        </form>
      </Modal>
    </div>
  );
}
