import React, { useEffect, useState } from "react";
import Dashboard from "../components/Dashboard";
import { useUser } from "../hooks/useUser";
import { Plus } from "lucide-react";
import CategoryList from "../components/CategoryList";
import axiosConfig from "../util/axiosConfig";
import { API_ENDPOINTS } from "../util/apiEndpoints";
import toast from "react-hot-toast";
import Model from "../components/Model";
import AddCategoryForm from "../components/AddCategoryForm";

function Category() {
  useUser();
  const [loading, setLoading] = useState(false);
  const [categoryData, setCategoryData] = useState([]);
  const [openAddCategoryModel, setOpenAddCategoryModel] = useState(false);
  const [openEditCategoryModel, setOpenEditCategoryModel] = useState(false);
  const [selectedCategory, setSelectedCategory] = useState(null);

  const fetchCategoryDetails = async () => {
    if (loading) return;
    setLoading(true);

    try {
      const response = await axiosConfig.get(API_ENDPOINTS.GET_ALL_CATEGORIES);
      if (response.status === 200) {
        console.log("Categories: ", response.data);
        setCategoryData(response.data);
      }
    } catch (error) {
      console.error("Something went wrong. Please try again later.", error);
      toast.error(error.message);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchCategoryDetails();
  }, []);

  const handleAddCategory = async (category) => {
    const { name, type, icon } = category;

    if (!name.trim()) {
      toast.error("Category name is required.");
      return;
    }

    // check if the category already exists
    const isDuplicate = categoryData.some((category) => {
      return category.name.toLowerCase() === name.trim().toLowerCase();
    });

    if (isDuplicate) {
      toast.error("Category name already exists.");
      return;
    }

    try {
      const response = await axiosConfig.post(API_ENDPOINTS.ADD_CATEGORY, {
        name,
        type,
        icon,
      });
      if (response.status === 201) {
        toast.success("Category added successfully!");
        setOpenAddCategoryModel(false);
        fetchCategoryDetails();
      }
    } catch (error) {
      console.error("Error adding category: ", error);
      toast.error(error.response.data.message || "Failed to add category.");
    }
  };

  const handleEditCategory = (categoryToEdit) => {
    setSelectedCategory(categoryToEdit);
    setOpenEditCategoryModel(true);
  };

  const handleUpdateCategory = async (updatedCategory) => {
    const { id, name, type, icon } = updatedCategory;
    if (!name.trim()) {
      toast.error("Category name is required.");
      return;
    }

    if (!id) {
      toast.error("Unable to update the category. Please try again.");
      return;
    }

    try {
      await axiosConfig.put(API_ENDPOINTS.UPDATE_CATEGORY(id), {
        name,
        type,
        icon,
      });
      setOpenEditCategoryModel(false);
      setSelectedCategory(null);
      toast.success("Category updated successfully!");
      fetchCategoryDetails();
    } catch (error) {
      console.error(
        "Error updating category: ",
        error.response.data.message || error.message
      );
      toast.error(error.response.data.message || "Failed to update category.");
    }
  };

  return (
    <div>
      <Dashboard activeMenu="Category">
        <div className="my-5 mx-auto">
          {/* Button to add category list */}
          <div className="flex justify-between items-center mb-5">
            <h2 className="text-2xl font-semibold">All Categories</h2>
            <button
              onClick={() => setOpenAddCategoryModel(true)}
              className="add-btn"
            >
              <Plus size={15} /> Add Category
            </button>
          </div>

          {/* Category list */}
          <CategoryList
            categories={categoryData}
            onEditCategory={handleEditCategory}
          />

          {/* Add category model */}
          <Model
            title="Add Category"
            isOpen={openAddCategoryModel}
            onClose={() => setOpenAddCategoryModel(false)}
          >
            <AddCategoryForm onAddCategory={handleAddCategory} />
          </Model>

          {/* Edit category model */}
          <Model
            title="Edit Category"
            isOpen={openEditCategoryModel}
            onClose={() => {
              setOpenEditCategoryModel(false);
              setSelectedCategory(null);
            }}
          >
            <AddCategoryForm
              initialCategoryData={selectedCategory}
              onAddCategory={handleUpdateCategory}
              isEditing={true}
            />
          </Model>
        </div>
      </Dashboard>
    </div>
  );
}

export default Category;
