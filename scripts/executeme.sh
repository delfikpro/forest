
FOREST_HOME_NEW="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"

echo "export \$FOREST_HOME=$FOREST_HOME_NEW" >> ~/.bashrc
echo "export \$PATH=\$PATH:$FOREST_HOME_NEW" >> ~/.bashrc

# shellcheck source=/dev/null
source ~/.bashrc

rm "$0"