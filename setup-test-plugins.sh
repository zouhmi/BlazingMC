#!/usr/bin/env bash
set -euo pipefail

source_dir="${1:-/home/zouhmi/Downloads/Plugins}"
target_dir="${2:-plugins}"

if [[ ! -d "$source_dir" ]]; then
    printf 'Plugin source directory does not exist: %s\n' "$source_dir" >&2
    exit 1
fi

mkdir -p "$target_dir"

plugin_files=(
    AntiMeteor.jar
    Chunky-Bukkit-1.4.40.jar
    CoreProtect-CE-24.0.jar
    FancyNpcs-2.11.0.jar
    FastAsyncWorldEdit-Paper-2.15.3.jar
    grimac-bukkit-2.3.73.jar
    instantrestock_2.6.4.jar
    InvSee++.jar
    LagFixer-1.7.1.jar
    LuckPerms-Bukkit-5.5.71.jar
    patpat-plugin-1.2.5.jar
    SkinsRestorer.jar
    TAB v6.1.2.jar
    veinminer-enchant-2.11.2+1.21.11.jar
    veinminer-paper-2.11.2+1.21.11.jar
    ViaVersion-5.11.0.jar
    voicechat-bukkit-2.6.21.jar
)

for plugin_file in "${plugin_files[@]}"; do
    source_file="$source_dir/$plugin_file"
    if [[ ! -f "$source_file" ]]; then
        printf 'Missing plugin: %s\n' "$source_file" >&2
        exit 1
    fi
    cp -f "$source_file" "$target_dir/$plugin_file"
done

printf 'Installed %d plugin JARs into %s\n' "${#plugin_files[@]}" "$target_dir"
printf 'The duplicate Chunky-Bukkit-1.4.40(1).jar was intentionally excluded.\n'
