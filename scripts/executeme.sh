
[[ "${BASH_SOURCE[0]}" != "${0}" ]] || (printf "\nPlease run this script via source command:\n source executeme.sh\n\n" && exit 1)

FOREST_HOME_NEW="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"

chmod +x "$FOREST_HOME_NEW/forest"
chmod +x "$FOREST_HOME_NEW/berry"

echo "export FOREST_HOME=$FOREST_HOME_NEW" >> ~/.bashrc
echo "export PATH=\$PATH:$FOREST_HOME_NEW" >> ~/.bashrc

# shellcheck source=/dev/null
source ~/.bashrc

rm "$0"