import { Trash, Upload, User } from "lucide-react";
import React, { useRef, useState } from "react";

function ProfilePhotoSelector({ image, setImage }) {
  const inputRef = useRef(null);
  const [previewUrl, setPreviewUrl] = useState(null);

  const handleImageChange = (e) => {
    const file = e.target.files[0];
    if (file) {
      setImage(file);

      const preview = URL.createObjectURL(file);
      setPreviewUrl(preview);
    }
  };

  const handleRemoveImage = (e) => {
    e.preventDefault();
    setImage(null);
    setPreviewUrl(null);
  };

  const onChooseFile = (e) => {
    e.preventDefault();
    inputRef.current.click();
  };

  return (
    <div className="flex justify-center mb-6">
      <input
        type="file"
        accept="image/*"
        ref={inputRef}
        onChange={handleImageChange}
        className="hidden"
      />

      {!image ? (
        <div className="w-20 h-20 flex items-center justify-center bg-blue-100 rounded-full relative">
          <User className="text-blue-500" size={35} />

          <button
            onClick={onChooseFile}
            className="w-8 h-8 flex items-center justify-center bg-blue text-white rounded-full absolute -bottom-1 -right-1"
          >
            <Upload size={15} className="text-blue-500 cursor-pointer" />
          </button>
        </div>
      ) : (
        <div className="relative">
          <img
            src={previewUrl}
            alt="Profil Photo"
            className="w-20 h-20 rounded-full object-cover"
          />
          <button
            onClick={handleRemoveImage}
            className="w-8 h-8 flex items-center justify-center bg-red-600 text-white rounded-full absolute -bottom-1 -right-1"
          >
            <Trash size={15} className="cursor-pointer" />
          </button>
        </div>
      )}
    </div>
  );
}

export default ProfilePhotoSelector;
