#!/bin/bash

# Exit immediately if a command exits with a non-zero status
set -e

echo "=== Starting Documentation Compilation ==="

# Check if npm is installed
if ! command -v npm &> /dev/null; then
    echo "Error: npm is not installed. Please install Node.js and npm first."
    exit 1
fi

compile_file() {
    local input_file="$1"
    local output_pdf="$2"
    local temp_md="temp_compiled.md"

    echo "--------------------------------------------------"
    echo "Compiling: $input_file -> $output_pdf"
    echo "--------------------------------------------------"

    # Step 1: Render Mermaid diagrams into SVGs using mermaid-cli
    echo "Step 1: Extracting and compiling Mermaid diagrams..."
    npx -y -p @mermaid-js/mermaid-cli mmdc -i "$input_file" -o "$temp_md"

    # Step 2: Convert compiled markdown to PDF with custom CSS styling and options
    echo "Step 2: Generating styled PDF from Markdown..."
    npx -y md-to-pdf "$temp_md" \
        --stylesheet pdf-style.css \
        --pdf-options '{
            "format": "A4",
            "margin": {
                "top": "25mm",
                "bottom": "25mm",
                "left": "20mm",
                "right": "20mm"
            },
            "displayHeaderFooter": true,
            "headerTemplate": "<div style=\"font-size: 8px; font-family: sans-serif; color: #94a3b8; width: 100%; text-align: right; padding-right: 20mm;\">Telemedicine System - Technical Documentation</div>",
            "footerTemplate": "<div style=\"font-size: 8px; font-family: sans-serif; color: #94a3b8; width: 100%; text-align: center;\">Page <span class=\"pageNumber\"></span> of <span class=\"totalPages\"></span></div>"
        }'

    # Step 3: Rename and Clean up
    echo "Step 3: Cleaning up temporary files..."
    mv temp_compiled.pdf "$output_pdf"
    rm -f temp_compiled*
}

# Compile English documentation
if [ -f "documentation.md" ]; then
    compile_file "documentation.md" "documentation.pdf"
fi

# Compile Italian documentation
if [ -f "documentazione-it.md" ]; then
    compile_file "documentazione-it.md" "documentazione-it.pdf"
fi

echo "=== All Compilations Complete! ==="
