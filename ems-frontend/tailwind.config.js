/** @type {import('tailwindcss').Config} */
module.exports = {
  content: ["./src/**/*.{html,ts}"],
  important: true, // Add this to make Tailwind more specific
  corePlugins: {
    preflight: false, // Disable Tailwind's reset to avoid conflicts
  },
  theme: {
    extend: {},
  },
  plugins: [],
};
