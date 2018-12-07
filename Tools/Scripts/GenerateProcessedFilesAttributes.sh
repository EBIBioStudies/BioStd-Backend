#!/usr/bin/env bash
# $1 -> path for the files
# $2 -> regex to look for the processed files
# $3 -> base path

find $1 -regex $2 -printf '%P,%f,%y,%s\n' >> files.list;
tsvOutput="processed.tsv";
rm -rf ${tsvOutput}

echo -e "Files\tDescription\tTime (minutes)\tImage Number" >> ${tsvOutput}

while IFS=',' read -r name file type size
do
    description=""
    time=""
    imageNumber=""
    if [[ "$type" == "f" ]];
    then
        case ${file} in
            M_Lab_*.mat) description="Matlab files containing all the M_Lab data";;
            M_Lab_*_CellInfo.csv) description="5 cell properties for each cell: 1,2: cell centroid x and y; 3: cell major axis; 4; cell minor axis; 5: Area of the cell";;
            M_Lab_*_Cells.tif) description="Image of segmented cells";;
            M_Lab_*_trans_plane.tif) description="Processed bright field image for cell segmentation";;
            M_nuclei_*.mat) description="Matlab files containing all the M_nuclei data";;
            M_nuclei_*_nuclei.tif) description="Centroid image of the maximum intensity projection of the 3D segmented nuclei";;
            M_nuclei_*_nuclei3D.tiff) description="Image stack of the 3D segmented nuclei";;
            SD_mRNA_*.mat) description="Matlab files containing all the SD_mRNA data";;
            SD_mRNA_*_CELLmaxRNAtmr_mid.csv | SD_mRNA_*_CELLmaxRNAcy5_mid.csv) description="Column 1: Total number of RNA in the cell; Column 2: Total number of RNA in the cytoplasm; Column 3: Total number of RNA in the nucleus";;
            SD_mRNA_*_TMR3D3immax.tiff | SD_mRNA_*_CY53D3immax.tiff) description="Maximum intensity of the filtered RNA-FISH image for all detected RNA spots in the image stack";;
            SD_mRNA_*_TMR3Dfilter.tiff | SD_mRNA_*_CY53Dfilter.tiff) description="Gaussian smoothed and Laplacian of a gaussian filtered RNA FISH image stack";;
            SD_mRNA_*_TMRmax.tif | SD_mRNA_*_CY5max.tif) description="Maximum intensity of the raw RNA-FISH image for all detected RNA spots in the image stack";;
            SD_mRNA_*_TMRmaxF.tif | SD_mRNA_*_CY5maxF.tif) description="Maximum intensity of the filtered RNA-FISH image for all RNA spots in the image stack";;
            SD_mRNA_*_TMRmaxFimmax.tif | SD_mRNA_*_CY5maxFimmax.tif) description="Maximum intensity of the filtered RNA-FISH image for all detected RNA spots in the image stack";;
            Result_*.mat) description="RNA counts per cell for each experiment";;
            Result_*_RNA_CY5_cytoplasm.csv) description="Cy5 (CTT1) cytoplasmic RNA counts per cell for each experiment";;
            Result_*_RNA_CY5_nuclear.csv) description="Cy5 (CTT1) nuclear RNA counts per cell for each experiment";;
            Result_*_RNA_CY5_total.csv) description="Cy5 (CTT1) total RNA counts per cell for each experiment";;
            Result_*_RNA_TMR_cytoplasm.csv) description="TMR (STL1) cytoplasmic RNA counts per cell for each experiment";;
            Result_*_RNA_TMR_nuclear.csv) description="TMR (STL1) nuclear RNA counts per cell for each experiment";;
            Result_*_RNA_TMR_total.csv) description="TMR (STL1) total RNA counts per cell for each experiment";;
        esac

        IFS='_' read -ra ADDR <<< "$file"
        for attr in "${ADDR[@]}"; do
            case ${attr} in
                *min) time=${attr%min*};;
                im*) imageNumber=${attr#*im};;
            esac
        done

        echo -e "$3/$name\t$description\t$time\t${imageNumber%.*}" >> ${tsvOutput}
    fi
done < files.list

rm -rf files.list